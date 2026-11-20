class Player extends Character{
private Inventory inventory;

    Player(String name, int health, int damage, int level) {
        super(name, health, damage, level);
        inventory = new Inventory();
        
    }

    
}