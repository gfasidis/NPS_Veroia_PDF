package sample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Player {

    private String number;
    private String name;
    private String position;
    private String code;

    public Player(String number, String name, String position, String code) {
        this.number = number;
        this.name = name;
        this.position = Objects.requireNonNullElse(position, "----");
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Number = " + number
                + " Name = " + name
                + " Position = " + position + "\n";
    }

    public static boolean containCaptain(ArrayList<Player> players, String captainNo){
        for (Player player : players) {
            if (captainNo.equals(player.number))
                return true;
        }
        return false;
    }

    public static boolean oneGoalKeeper(ArrayList<Player> players){
        int i = 0;
        for (Player player : players) {
            if ("ΤΕΡΜ.".equals(player.position))
                i++;
        }
        if (i != 1)
            return false;
        return true;
    }

    public static boolean containPlayer(ArrayList<Player> players, String playerNo){
        for (Player player : players) {
            if (playerNo.equals(player.number))
                return true;
        }
        return false;
    }

    public static Player getGK(ArrayList<Player> players){
        for (Player player : players){
            if (player.getPosition().equals("ΤΕΡΜ."))
                return player;
        }
        return null;
    }

    public static Comparator<Player> playersComparator = (p1, p2) -> {

        int player1No = Integer.parseInt(p1.getNumber());
        int player2No = Integer.parseInt(p2.getNumber());

        return player1No - player2No;

    };

}
