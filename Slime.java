class Slime extends Monster{

    Slime(String name, int health, int damage, int level) {
        super(name, health, damage, level);
    }

    @Override
    void attack(Character target){
    System.out.println(getName() + " " + "attacks" + " " + target.getName());
    target.takeDamage(10);
    }
    
}