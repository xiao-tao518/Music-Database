DROP DATABASE finalproject;
CREATE DATABASE IF NOT EXISTS finalproject;

USE finalproject;

CREATE TABLE artist
( artist_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  artist_name VARCHAR(30) NOT NULL
  );
  
INSERT INTO artist(artist_name) VALUE ("Paul Potts");
INSERT INTO artist(artist_name) VALUE ("Willie Nelson");
INSERT INTO artist(artist_name) VALUE ("Miles Davis");
INSERT INTO artist(artist_name) VALUE ("Wiz Khalifa");
INSERT INTO artist(artist_name) VALUE ("Corey Taylor");
  
CREATE TABLE genre
( genre_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  genre_name VARCHAR(30) NOT NULL
  );
  
INSERT INTO genre(genre_name) VALUE ("classical");
INSERT INTO genre(genre_name) VALUE ("country");
INSERT INTO genre(genre_name) VALUE ("jazz");
INSERT INTO genre(genre_name) VALUE ("pop");
INSERT INTO genre(genre_name) VALUE ("rock");

  
CREATE TABLE song
( song_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  song_name VARCHAR(30) NOT NULL,
  song_length VARCHAR(30) NOT NULL,
  artist_id int NOT NULL,
  genre_id int NOT NULL,
  FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT,
  FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT
  );
  
INSERT INTO song(song_name, song_length, artist_id, genre_id) VALUE ("Nessun Dorma", "2:55", 1, 1);
INSERT INTO song(song_name, song_length, artist_id, genre_id) VALUE ("Night Life", "2:35", 2, 2);
INSERT INTO song(song_name, song_length, artist_id, genre_id) VALUE ("So What", "3:08", 3, 3);
INSERT INTO song(song_name, song_length, artist_id, genre_id) VALUE ("See You Again", "3:49", 4, 4);
INSERT INTO song(song_name, song_length, artist_id, genre_id) VALUE ("Black Eyes Blue", "3:23", 5, 5);
  
CREATE TABLE users
( user_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  user_name VARCHAR(30) NOT NULL,
  user_password VARCHAR(30) NOT NULL
  );
  
INSERT INTO users(user_name, user_password) VALUE ("root", "root");
INSERT INTO users(user_name, user_password) VALUE ("user1", "user1");
  
CREATE TABLE comments
( comment_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  comment_description VARCHAR(255) NOT NULL,
  song_id int NOT NULL,
  user_id int NOT NULL,
  FOREIGN KEY (song_id) REFERENCES song(song_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT
  );
  
CREATE TABLE likes
( like_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  song_id int NOT NULL,
  user_id int NOT NULL,
  FOREIGN KEY (song_id) REFERENCES song(song_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT
  );
  
CREATE TABLE dislikes
( dislike_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  song_id int NOT NULL,
  user_id int NOT NULL,
  FOREIGN KEY (song_id) REFERENCES song(song_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
      ON UPDATE RESTRICT ON DELETE RESTRICT
  );
  
DROP PROCEDURE IF EXISTS like_song
DELIMITER //
CREATE PROCEDURE like_song(IN songID int, IN userID int)
BEGIN
  IF (NOT EXISTS (SELECT * FROM likes WHERE song_id = songID AND user_id = userID))
  THEN
  INSERT INTO likes(song_id, user_id) VALUE (songID, userID);
  END IF;
  SET SQL_SAFE_UPDATES=0;
  DELETE FROM dislikes WHERE user_id = userID and song_id = songID;
  SET SQL_SAFE_UPDATES = 1;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS dislike_song
DELIMITER //
CREATE PROCEDURE dislike_song(IN songID int, IN userID int)
BEGIN
  IF (NOT EXISTS (SELECT * FROM dislikes WHERE song_id = songID AND user_id = userID))
  THEN
  INSERT INTO dislikes(song_id, user_id) VALUE (songID, userID);
  END IF;
  SET SQL_SAFE_UPDATES=0;
  DELETE FROM likes WHERE user_id = userID and song_id = songID;
  SET SQL_SAFE_UPDATES = 1;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS comment_song
DELIMITER //
CREATE PROCEDURE comment_song(IN songID int, IN userID int, IN user_comment VARCHAR(255))
BEGIN
  IF (NOT EXISTS (SELECT * FROM comments WHERE song_id = songID AND user_id = userID)) THEN
    INSERT INTO comments(song_id, user_id, comment_description) VALUE (songID, userID, user_comment);
  ELSE
    UPDATE comments
    SET comment_description = user_comment WHERE songID = song_id AND userID = user_id;
  END IF;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS delete_comment
DELIMITER //
CREATE PROCEDURE delete_comment(IN songID int, IN userID int)
BEGIN
  SET SQL_SAFE_UPDATES=0;
  DELETE FROM comments WHERE user_id = userID and song_id = songID;
  SET SQL_SAFE_UPDATES = 1;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS show_comment
DELIMITER //
CREATE PROCEDURE show_comment(IN songID int)
BEGIN
  SELECT * FROM comments
  JOIN
  (SELECT song_name, song_id as sid FROM song)tb
  ON song_id = sid
  JOIN
  (SELECT user_name, user_id as uid FROM users)tb2
  ON user_id = uid
   WHERE song_id = songID;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS show_songs
DELIMITER //
CREATE PROCEDURE show_songs()
BEGIN
  SELECT song_id, song_name, song_length, genre_name AS genre, artist_name AS artist FROM 
  song
  LEFT JOIN
  genre
  ON song.genre_id = genre.genre_id
  JOIN
  artist
  ON song.artist_id = artist.artist_id;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS like_list
DELIMITER //
CREATE PROCEDURE like_list(IN uid int)
BEGIN
  SELECT likes.song_id AS song_id, song_name FROM likes 
  LEFT JOIN
  song
  ON likes.song_id = song.song_id
  WHERE user_id = uid
  ORDER BY song_id ASC;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS dislike_list
DELIMITER //
CREATE PROCEDURE dislike_list(IN uid int)
BEGIN
  SELECT dislikes.song_id AS song_id, song_name FROM 
  dislikes
  LEFT JOIN
  song
  ON dislikes.song_id = song.song_id
  WHERE user_id = uid
  ORDER BY song_id ASC;
END //
DELIMITER ;
