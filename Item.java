class Item{
private String name;
private String description;
private int price;

    public Item(String name, String description, int price) {

    this.name = name;

    this.description = description;

    if(price< 0){
    throw new IllegalArgumentException("Price cannot be negative");
    }
    this.price= price;
}


String getName(){
return name;}

void setName(String name){
this.name= name;}

String getDescription(){
return description;}

void setDescription(String description){
this.description= description;
}

int getPrice(){
return price;
}

void setPrice(int price){
if(price< 0){
    throw new IllegalArgumentException("Price cannot be negative");
    }
    this.price= price;
}

void displayInfo(){
System.out.println("Name: " + name + "|" + "Description: " + description + "|" + "Price: " + price);
}
}