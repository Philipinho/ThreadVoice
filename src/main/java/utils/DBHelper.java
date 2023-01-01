package utils;

import java.sql.*;

public class DBHelper {
    private static Connection connection;
    private static final String DATABASE = ReadProperty.getValue("mysql.db");

    public static void saveTweet(String username, String mentionId, String threadId, String voiceUrl, String threadAuthor, String threadExcerpt, String threadLanguage) {
        String sql = "INSERT INTO tweet_records(username, mention_id, thread_id, voice_url, thread_author, thread_excerpt, thread_language) VALUES(?,?,?,?,?,?,?)";
        try {
            connection = DriverManager.getConnection(DATABASE);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, mentionId);
            ps.setString(3, threadId);
            ps.setString(4, voiceUrl);
            ps.setString(5, threadAuthor);
            ps.setString(6, threadExcerpt);
            ps.setString(7, threadLanguage);
            ps.executeUpdate();
            connection.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getThreadVoiceName(String threadId) {
        StringBuilder voiceName = new StringBuilder();
        try {
            connection = DriverManager.getConnection(DATABASE);
            PreparedStatement ps = connection.prepareStatement("SELECT voice_url FROM tweet_records WHERE thread_id = ?");
            ps.setString(1, threadId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                voiceName.append(rs.getString("voice_url"));
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voiceName.toString();
    }

    public static boolean isThreadExists(String threadId){
        boolean response = false;
        try {
            connection = DriverManager.getConnection(DATABASE);
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM tweet_records WHERE thread_id = ?");
            ps.setString(1, threadId);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    boolean found = rs.getBoolean(1);

                    if (found){
                        response = true;
                    }
                }
            }

            connection.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return response;
    }

}