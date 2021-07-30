import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


/**
 * Modified based on starter code from cs3200 database design
 *
 * @author kath
 */
public class Finalproject {

  /**
   * The name of the MySQL account to use (or empty for anonymous)
   */
  private static String userName; // "root";

  /**
   * The password for the MySQL account (or empty for anonymous)
   */
  private static String password; // "password";

  /**
   * The name of the computer running MySQL
   */
  private final String serverName = "localhost";

  /**
   * The port of the MySQL server (default is 3306)
   */
  private final int portNumber = 3306;

  /**
   * The name of the database we are testing with (this default is installed with MySQL)
   */
  private final String dbName = "finalproject";

  private final boolean useSSL = false;

  private static String userAccount;

  private static int userID;

  /**
   * Get a new database connection
   *
   * @return
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
    Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", this.userName);
    connectionProps.put("password", this.password);

    conn = DriverManager.getConnection("jdbc:mysql://"
                    + this.serverName + ":" + this.portNumber + "/" + this.dbName + "?allowPublicKeyRetrieval" +
                    "=true&characterEncoding=UTF-8&useSSL=false",
            connectionProps);

    return conn;
  }

  /**
   * Connect to MySQL and do some stuff.
   */
  public void run() {
    boolean login = false;

    Scanner scanner = new Scanner(System.in);
    System.out.print("MySQL Username: ");
    userName = scanner.next();
    System.out.print("MySQL Password: ");
    password = scanner.next();

    // Connect to MySQL
    Connection conn = null;
    try {
      conn = this.getConnection();
      System.out.println("Connected to database\n");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not connect to the database");
      e.printStackTrace();
      return;
    }

    String pass = "";
    while (!login) {
      System.out.println("Account Username: ");
      userAccount = scanner.next();
      System.out.println("Account Password: ");
      pass = scanner.next();
      try {
        Statement getAccount = conn.createStatement();
        String query =
                "SELECT user_id, user_name, user_password From users where user_name = \"" + userAccount +
                        "\"";
        ResultSet rs = getAccount.executeQuery(query);

        if (rs.next()) {
          while (true) {
            if (rs.getString("user_name").equals(userAccount)) {
              if (rs.getString("user_password").equals(pass)) {
                login = true;
                userID = rs.getInt("user_id");
                System.out.println("Welcome back, " + userAccount + "\n");
                break;
              } else {
                System.out.println("incorrect password\n");
                break;
              }
            }
            if (!rs.next()) {
              break;
            }
          }
        } else {
          System.out.println("Account does not exist\n");
        }
      } catch (SQLException e) {
        System.out.println(e);
      }
    }

    int count = 0;
    try {
      Statement getCharacterName = conn.createStatement();
      String query = "CALL show_songs()";
      ResultSet rs = getCharacterName.executeQuery(query);
      System.out.println("song list: ");
      while (rs.next()) {
        count++;
        String id = rs.getString(("song_id"));
        String name = rs.getString("song_name");
        String length = rs.getString("song_length");
        String genre = rs.getString("genre");
        String artist = rs.getString("artist");
        System.out.println(id + ". " + name + " | length: " + length + ", genre: " + genre + ", " +
                "artist: " + artist);
      }
      getCharacterName.close();
    } catch (
            SQLException e) {
      System.out.print("Get song list failed");
    }

    boolean exit = false;
    while (!exit) {
      System.out.println("\n\nPress 1 to like a song, press 2 to dislike a song, press 3 to see " +
              "song's comments, press 4 to comment a song, press 5 to delete a comment, press 6 " +
              "to log out and exit");
      String choice = scanner.next();
      switch (choice) {
        case "1":
          likeSong(conn, count);
          break;
        case "2":
          dislikeSong(conn, count);
          break;
        case "3":
          showComments(conn, count);
          break;
        case "4":
          commentSong(conn, count);
          break;
        case "5":
          deleteComment(conn, count);
          break;
        case "6":
          try {
            conn.close();
            exit = true;
            System.out.println("Bye bye!");
            break;
          } catch (SQLException e) {
            System.out.print(e);
          }
      }
    }
  }

  private void likeSong(Connection conn, int count) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Give the song number to like the song: ");
      int songID = Integer.parseInt(scanner.next());
      if (songID <= count) {
        CallableStatement callLikeSong = null;
        CallableStatement callLikeList = null;
        try {
          callLikeSong = conn.prepareCall("{CALL like_song(?,?)}");
          callLikeSong.setInt(1, songID);
          callLikeSong.setInt(2, userID);

          callLikeSong.execute();
          callLikeSong.close();
          System.out.println("You have added song " + songID + " to your like list\n");

          callLikeList = conn.prepareCall("{CALL like_list(?)}");
          callLikeList.setInt(1, userID);
          boolean hadResults = callLikeList.execute();
          if (hadResults) {
            System.out.println("Current like list: ");
            ResultSet rs = callLikeList.getResultSet();
            while (rs.next()) {
              int sid = rs.getInt("song_id");
              String sname = rs.getString("song_name");
              System.out.println(sid + " " + sname);
            }
          }
          callLikeList.close();
          break;
        } catch (SQLException e) {
          System.out.println(e);
        }
      } else {
        System.out.println("Song not found, check song list and try again!");
      }
    }
  }

  private void dislikeSong(Connection conn, int count) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Give the song number to dislike the song: ");
      int songID = Integer.parseInt(scanner.next());
      if (songID <= count) {
        CallableStatement callDislikeSong = null;
        CallableStatement callDislikeList = null;
        try {
          callDislikeSong = conn.prepareCall("{CALL dislike_song(?,?)}");
          callDislikeSong.setInt(1, songID);
          callDislikeSong.setInt(2, userID);

          callDislikeSong.execute();
          callDislikeSong.close();
          System.out.println("You have added song " + songID + " to your dislike list");

          callDislikeList = conn.prepareCall("{CALL dislike_list(?)}");
          callDislikeList.setInt(1, userID);
          boolean hadResults = callDislikeList.execute();
          if (hadResults) {
            System.out.println("Current dislike list: ");
            ResultSet rs = callDislikeList.getResultSet();
            while (rs.next()) {
              int sid = rs.getInt("song_id");
              String sname = rs.getString("song_name");
              System.out.println(sid + " " + sname);
            }
          }
          callDislikeList.close();
          break;
        } catch (SQLException e) {
          System.out.println(e);
        }
      } else {
        System.out.println("Song not found, check song list and try again!");
      }
    }
  }

  private void showComments(Connection conn, int count) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Give the song number to see comments of the song: ");
      int songID = Integer.parseInt(scanner.next());
      if (songID <= count) {
        CallableStatement callShowComments = null;
        try {
          callShowComments = conn.prepareCall("{CALL show_comment(?)}");
          callShowComments.setInt(1, songID);

          boolean hadResults = callShowComments.execute();
          if (hadResults) {
            System.out.println("Comments for song " + songID + ": ");
            ResultSet rs = callShowComments.getResultSet();
            while (rs.next()) {
              String username = rs.getString("user_name");
              String description = rs.getString("comment_description");
              System.out.println(username + ": " + description);
            }
          } else {
            System.out.println("No comment found for this song");
          }
          callShowComments.close();
          break;
        } catch (SQLException e) {
          System.out.println(e);
        }
      } else {
        System.out.println("Song not found, check song list and try again!");
      }
    }
  }

  private void commentSong(Connection conn, int count) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Give the song number to comment: ");
      int songID = Integer.parseInt(scanner.next());
      scanner.nextLine();
      System.out.println("Type your new comment below: ");
      String comment = scanner.nextLine();
      if (songID <= count) {
        CallableStatement callCommentSong = null;
        try {
          callCommentSong = conn.prepareCall("{CALL comment_song(?,?,?)}");
          callCommentSong.setInt(1, songID);
          callCommentSong.setInt(2, userID);
          callCommentSong.setString(3, comment);

          callCommentSong.execute();
          callCommentSong.close();
          System.out.println("You have successfully create a comment for song " + songID);
          break;
        } catch (SQLException e) {
          System.out.println(e);
        }
      } else {
        System.out.println("Song not found, check song list and try again!");
      }
    }
  }

  private void deleteComment(Connection conn, int count) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Give the song number to delete comment: ");
      int songID = Integer.parseInt(scanner.next());
      if (songID <= count) {
        CallableStatement callDeleteComment = null;
        try {
          callDeleteComment = conn.prepareCall("{CALL delete_comment(?,?)}");
          callDeleteComment.setInt(1, songID);
          callDeleteComment.setInt(2, userID);

          callDeleteComment.execute();
          callDeleteComment.close();
          System.out.println("You have successfully delete the comment for song " + songID);
          break;
        } catch (SQLException e) {
          System.out.println(e);
        }
      } else {
        System.out.println("Song not found, check song list and try again!");
      }
    }
  }


  /**
   * Connect to the DB and do some stuff
   *
   * @param args
   */
  public static void main(String[] args) {
    Finalproject app = new Finalproject();
    app.run();
  }
}

