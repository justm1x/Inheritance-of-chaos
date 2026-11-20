class Location{
    private String name;
    private int difficulty, minLevel, maxLevel;

    Location(String name, int difficulty, int minLevel, int maxLevel) {

        this.name=name;
        if(difficulty < 0){
            throw new IllegalArgumentException("Difficulty cannot be negative");
        }
        this.difficulty=difficulty;

        if (minLevel < 1){
            throw new IllegalArgumentException("MinLevel cannot be negative");
        }
        this.minLevel=minLevel;

        if (maxLevel < minLevel){
            throw new IllegalArgumentException("MaxLevel cannot be smaller then MinLevel");
        }
        this.maxLevel=maxLevel;
    }

    String getName() {
        return name;
    }

    void setName(String name){
        this.name = name;
    }

    int getDifficulty(){
        return difficulty;
    }

    void setDifficulty(int difficulty){
        this.difficulty=difficulty;
    }

    int getMinLevel(){
        return minLevel;
    }

    void setMinLevel(int minLevel){
        this.minLevel=minLevel;
    }

    int getMaxLevel(){
        return maxLevel;
    }

    void setMaxLevel(int maxLevel){
        this.maxLevel=maxLevel;
    }

    void displayInfo(){
        System.out.println("Location: " + name + " | " + "Difficulty: " + difficulty + " | " + "Monster Levels: " + minLevel + "-" + maxLevel);
    }
    
}