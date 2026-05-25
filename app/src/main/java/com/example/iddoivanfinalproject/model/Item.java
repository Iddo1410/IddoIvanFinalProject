// הגדרת החבילה (Package) שבה נמצא הקובץ, תחת תיקיית המודלים (model)
package com.example.iddoivanfinalproject.model;

// הגדרת המחלקה Item, המייצגת אובייקט של פריט (מוצר) בודד בחנות או במערכת
public class Item {

    // --- מאפייני המחלקה (משתנים) ---
    // המשתנים מוגדרים כ-private כדי לשמור על עקרון הכימוס (Encapsulation) ולמנוע גישה ישירה אליהם מבחוץ
    private String id;       // מזהה ייחודי (ID) של המוצר
    private String name;     // שם המוצר
    private String type;     // סוג או קטגוריית המוצר (למשל: רכב, סמארטפון וכו')
    private String brand;    // המותג של המוצר (למשל: סמסונג, אפל, טויוטה)
    private double price;    // מחיר המוצר (מספר עשרוני)
    private String year;     // שנת הייצור של המוצר
    private String details;  // תיאור או פרטים נוספים על המוצר
    private String pic;      // תמונת המוצר (נשמרת כמחרוזת טקסט, בדרך כלל בפורמט Base64)

    // בנאי מלא (Constructor). 
    // מופעל כשרוצים ליצור מופע חדש של מוצר ולהעביר אליו את כל הנתונים בבת אחת (למשל בעת יצירת מוצר חדש באפליקציה)
    public Item(String id, String name, String type, String brand, double price, String year, String details, String pic) {
        this.id = id;             // השמת מזהה המוצר שהתקבל לתוך המשתנה של המחלקה (this מציין את האובייקט הנוכחי)
        this.name = name;         // השמת שם המוצר
        this.type = type;         // השמת סוג המוצר
        this.brand = brand;       // השמת מותג המוצר
        this.price = price;       // השמת מחיר המוצר
        this.year = year;         // השמת שנת הייצור
        this.details = details;   // השמת תיאור המוצר
        this.pic = pic;           // השמת תמונת המוצר
    }

    // בנאי ריק (Empty Constructor).
    // חובה להגדיר אותו כשעובדים עם מסדי נתונים (כמו Firebase), כדי שהמערכת תוכל ליצור אובייקט ריק ורק לאחר מכן לצקת לתוכו את הנתונים שנשלפו.
    public Item() {
    }

    // --- מתודות Getters ו-Setters המאפשרות לקרוא (Get) ולעדכן (Set) את הנתונים באופן מבוקר ---

    // מתודה המחזירה את מזהה המוצר (ID)
    public String getId() {
        return id;
    }

    // מתודה המעדכנת את מזהה המוצר
    public void setId(String id) {
        this.id = id;
    }

    // מתודה המחזירה את שם המוצר
    public String getName() {
        return name;
    }

    // מתודה המעדכנת את שם המוצר
    public void setName(String name) {
        this.name = name;
    }

    // מתודה המחזירה את סוג/קטגוריית המוצר
    public String getType() {
        return type;
    }

    // מתודה המעדכנת את סוג המוצר
    public void setType(String type) {
        this.type = type;
    }

    // מתודה המחזירה את מותג המוצר
    public String getBrand() {
        return brand;
    }

    // מתודה המעדכנת את מותג המוצר
    public void setBrand(String brand) {
        this.brand = brand;
    }

    // מתודה המחזירה את מחיר המוצר
    public double getPrice() {
        return price;
    }

    // מתודה המעדכנת את מחיר המוצר
    public void setPrice(double price) {
        this.price = price;
    }

    // מתודה המחזירה את שנת הייצור של המוצר
    public String getYear() {
        return year;
    }

    // מתודה המעדכנת את שנת הייצור של המוצר
    public void setYear(String year) {
        this.year = year;
    }

    // מתודה המחזירה את תיאור המוצר
    public String getDetails() {
        return details;
    }

    // מתודה המעדכנת את תיאור המוצר
    public void setDetails(String details) {
        this.details = details;
    }

    // מתודה המחזירה את תמונת המוצר (כמחרוזת טקסט)
    public String getPic() {
        return pic;
    }

    // מתודה המעדכנת את תמונת המוצר
    public void setPic(String pic) {
        this.pic = pic;
    }

    // דריסה (Override) של המתודה toString.
    // מתודה זו אורזת את כל פרטי המוצר לתוך מחרוזת טקסט אחת ארוכה. 
    // שימושי מאוד להדפסת האובייקט לקונסול (Log) לצורכי בדיקות (Debugging).
    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", year='" + year + '\'' +
                ", details='" + details + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}