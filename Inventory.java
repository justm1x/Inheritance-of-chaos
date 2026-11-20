import java.util.ArrayList;

class Inventory{
    private ArrayList<Item> items;
    public Inventory(){
        items = new ArrayList<>();

    }

    void addItem(Item newItem){
        items.add(newItem);   
    }

    void removeItem(Item newItem){
        items.remove(newItem);
    }

    void displayInfo(){
        for (int i = 0; i < items.size(); i++) {
            items.get(i).displayInfo(); 
        }
    }
}