class Character{
    private int health, maxHealth, damage, level;
    private String name;

    Character(String name, int health, int damage, int level) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.level= level;
        
    }

    int getHealth(){
        return health;
    }
    void setHealth(int health){
        this.health = health;
    }

    String getName(){
        return name;
    }

    void setName(String name){
        this.name = name;
    }

    int getDamage(){
        return damage;
    }
    void setDamage(int damage){
        this.damage = damage;
    }

    int getLevel(){
        return level;
    }
    void setLevel(int level){
        this.level = level;
    }

    int takeDamage(int damage){
        if(damage < 0){
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        int oldHealth = health;
        health -= damage;
        
        if (health < 0 ){
            health = 0;
        }
        int actualDamage = oldHealth - health;


        return actualDamage;
    }

    void healHP(int heal){
        if(heal < 0){
            throw new IllegalArgumentException("Healling cannot be negative");
            }

        health += heal;

        if(health > maxHealth){
            health = maxHealth;
        }
    }

    void displayInfo(){
        System.out.println("Monster: " + name + " | Health: " + health);
    } 

    void attack(Character target){
        System.out.println(name + " " + "attacks" + " " + target.name);
        target.takeDamage(20);
    }
}