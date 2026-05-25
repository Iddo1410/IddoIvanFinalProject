// הגדרת החבילה (Package) בה נמצא הקובץ, בתוך תיקיית המודלים (model) של הפרויקט
package com.example.iddoivanfinalproject.model;

// ייבוא מחלקת ArrayList מתוך ספריית הכלים של Java, המאפשרת יצירה ושימוש ברשימות דינמיות
import java.util.ArrayList;

// הגדרת המחלקה Compareitem, המייצגת אובייקט של "השוואת פריטים" במערכת
public class Compareitem {

    // --- מאפייני המחלקה (משתנים) ---
    // (הערה: נהוג בדרך כלל להגדיר משתנים אלו כ-private כדי למנוע גישה ישירה מבחוץ)

    String id;                           // מזהה ייחודי (ID) של פעולת ההשוואה
    String type;                         // סוג או קטגוריית ההשוואה (למשל: סמארטפונים, רכבים וכו')
    String date;                         // תאריך ביצוע או שמירת ההשוואה (שמור כמחרוזת טקסט)
    ArrayList<Item> itemArrayList;       // רשימה דינמית שמכילה את המוצרים עצמם (אובייקטים מסוג Item) שמשתתפים בהשוואה

    // בנאי מלא (Constructor). 
    // מופעל כאשר אנחנו רוצים ליצור אובייקט השוואה חדש ולהעביר אליו את כל הנתונים בבת אחת.
    public Compareitem(String id, String type, String date, ArrayList<Item> itemArrayList) {
        this.id = id;                       // השמת המזהה באובייקט הנוכחי
        this.type = type;                   // השמת סוג ההשוואה
        this.date = date;                   // השמת התאריך
        this.itemArrayList = itemArrayList; // השמת רשימת המוצרים
    }

    // בנאי ריק (Empty Constructor). 
    // חובה להגדיר אותו כאשר עובדים עם מסדי נתונים (כמו Firebase), כדי שהמערכת תוכל ליצור אובייקט ריק ולשפוך לתוכו נתונים מהשרת.
    public Compareitem() {
    }

    // --- מתודות Getters ו-Setters המאפשרות לקבל ולעדכן את הנתונים באופן מסודר ---

    // מתודה המחזירה את מזהה ההשוואה (id)
    public String getId() {
        return id;
    }

    // מתודה המעדכנת את מזהה ההשוואה
    public void setId(String id) {
        this.id = id;
    }

    // מתודה המחזירה את סוג ההשוואה
    public String getType() {
        return type;
    }

    // מתודה המעדכנת את סוג ההשוואה
    public void setType(String type) {
        this.type = type;
    }

    // מתודה המחזירה את התאריך בו בוצעה ההשוואה
    public String getDate() {
        return date;
    }

    // מתודה המעדכנת את תאריך ההשוואה
    public void setDate(String date) {
        this.date = date;
    }

    // מתודה המחזירה את רשימת המוצרים (Item) המשתתפים בהשוואה
    public ArrayList<Item> getItemArrayList() {
        return itemArrayList;
    }

    // מתודה המעדכנת/מכניסה רשימת מוצרים חדשה להשוואה
    public void setItemArrayList(ArrayList<Item> itemArrayList) {
        this.itemArrayList = itemArrayList;
    }

    // דריסה (Override) של המתודה toString.
    // פונקציה זו משמשת להחזרת מחרוזת טקסט המייצגת את כל פרטי האובייקט (id, סוג, תאריך ורשימת המוצרים).
    // שימושי מאוד במיוחד לצורכי דיבאגינג והדפסות ללוג (Logcat).
    @Override
    public String toString() {
        return "Compareitem{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", itemArrayList=" + itemArrayList +
                '}';
    }
}