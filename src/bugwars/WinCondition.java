package bugwars;

public enum WinCondition {
    DESTRUCTION ("Destruction"),
    NB_QUEENS ("Number of queens"),
    HP_QUEENS ("Total HP of queens"),
    VALUE ("Sum of all resources and units"),
    RANDOM ("Random"),
    SINGLE_PLAYER ("Single player"),
    CHEATING ("Opponent cheated!!!");


    String condition;

    WinCondition(String condition){
        this.condition = condition;
    }

}
