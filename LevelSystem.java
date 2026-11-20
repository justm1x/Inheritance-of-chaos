import java.util.Random;

class LevelSystem{

private Random random = new Random();

    public LevelSystem() {
    }


    int generateMonsterLevel(Player player, Location location){
        int min = location.getMinLevel();
        int max = location.getMaxLevel();

        int playerLevel = player.getLevel();

        int difficulty = location.getDifficulty();

        int randomValue = random.nextInt(3) - 1;

        int monsterLevel = playerLevel + difficulty + randomValue;

        if(monsterLevel < min){
            monsterLevel = min;
        }

        if (monsterLevel > max){
            monsterLevel = max;
        }

        return monsterLevel;
    }      
}