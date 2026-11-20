class Goblin extends Monster{

    Goblin(String name, int health, int damage, int level) {
        super(name, health, damage, level);
    }
    
    void stealHealth(Character target){
        System.out.println(getName() + " " + "Steals health from" + " " + target.getName());

        int damage = target.takeDamage(10);
        healHP(damage);
    }
}
