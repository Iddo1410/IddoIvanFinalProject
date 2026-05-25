// הגדרת החבילה (Package) שבה נמצא הקובץ, בתוך תיקיית המודלים (model)
package com.example.iddoivanfinalproject.model;

// הגדרת המחלקה Cart, המייצגת אובייקט של פריט בודד שנמצא בתוך עגלת הקניות
public class Cart {

    // --- הגדרת המאפיינים (משתנים) של הפריט. הם מוגדרים כ-private כדי למנוע גישה ישירה אליהם מבחוץ ---
    private String name;     // שם המוצר (מחרוזת טקסט)
    private double price;    // מחיר המוצר (מספר עשרוני)
    private int quantity;    // הכמות של המוצר בעגלה (מספר שלם)
    private String id;       // מזהה ייחודי (ID) של הפריט
    private String userId;   // מזהה (ID) של המשתמש (הקונה) שהפריט שייך אליו
    private String pic;      // התמונה של המוצר (בדרך כלל שמורה כמחרוזת בפורמט Base64)

    // בנאי ריק (Empty Constructor). 
    // חובה להגדיר אותו כאשר עובדים עם מסדי נתונים כמו Firebase, כדי שהמערכת תוכל ליצור אובייקט ריק ולמלא אותו בהמשך.
    public Cart() {
    }

    // בנאי מלא (Constructor). 
    // מופעל כאשר אנחנו רוצים ליצור פריט חדש לעגלה ולהעביר אליו את כל הנתונים בבת אחת.
    public Cart(String name, double price, int quantity, String id, String userId, String pic) {
        this.name = name;         // השמת שם המוצר שהתקבל במשתנה של המחלקה (this מציין את האובייקט הנוכחי)
        this.price = price;       // השמת המחיר
        this.quantity = quantity; // השמת הכמות
        this.id = id;             // השמת מזהה הפריט
        this.userId = userId;     // השמת מזהה המשתמש
        this.pic = pic;           // השמת התמונה
    }

    // --- מתודות Getters ו-Setters המאפשרות לקבל ולעדכן את הנתונים בצורה מבוקרת ---

    // מתודה המחזירה את שם המוצר
    public String getName() {
        return name;
    }

    // מתודה המעדכנת את שם המוצר
    public void setName(String name) {
        this.name = name;
    }

    // מתודה המחזירה את מחיר המוצר
    public double getPrice() {
        return price;
    }

    // מתודה המעדכנת את מחיר המוצר
    public void setPrice(double price) {
        // כאן הוספת בדיקת תקינות (וולידציה): המחיר יעודכן רק אם הוא גדול או שווה ל-0 (לא יכול להיות מחיר שלילי)
        if (price >= 0) {
            this.price = price;
        }
    }

    // מתודה המחזירה את הכמות של המוצר בעגלה
    public int getQuantity() {
        return quantity;
    }

    // מתודה המעדכנת את הכמות
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // מתודה המחזירה את ה-ID (המזהה) של הפריט
    public String getId() {
        return id;
    }

    // מתודה המעדכנת את ה-ID של הפריט
    public void setId(String id) {
        this.id = id;
    }

    // מתודה המחזירה את מזהה המשתמש (מי שהוסיף לעגלה)
    public String getUserId() {
        return userId;
    }

    // מתודה המעדכנת את מזהה המשתמש
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // מתודה המחזירה את התמונה (כמחרוזת)
    public String getPic() {
        return pic;
    }

    // מתודה המעדכנת את מחרוזת התמונה
    public void setPic(String pic) {
        this.pic = pic;
    }

    // דריסה (Override) של המתודה toString.
    // מתודה זו משמשת להדפסת האובייקט (למשל לצורך דיבאגינג בלוגים). היא מחזירה מחרוזת טקסט שמכילה את כל פרטי העגלה בפורמט קריא.
    @Override
    public String toString() {
        return "Cart{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}