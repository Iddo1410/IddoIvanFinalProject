// הגדרת החבילה (Package) שבה נמצא הקובץ, תחת תיקיית המודלים (model)
package com.example.iddoivanfinalproject.model;

// הגדרת המחלקה User, המייצגת משתמש במערכת (יכול להיות לקוח רגיל או מנהל)
public class User {

    // --- מאפייני המשתמש (המשתנים) ---
    // המשתנים מוגדרים כ-private כדי לשמור על אבטחה ועל עקרון הכימוס (Encapsulation), כך שלא ניתן לשנות אותם ישירות מבחוץ.
    private String id;           // מזהה ייחודי (ID) של המשתמש (בדרך כלל נוצר ומנוהל על ידי מערכת ההתחברות כמו Firebase)
    private String fname;        // השם הפרטי של המשתמש (First Name)
    private String lname;        // שם המשפחה של המשתמש (Last Name)
    private String email;        // כתובת האימייל של המשתמש
    private String phoneNumber;  // מספר הטלפון של המשתמש
    private String password;     // סיסמת המשתמש (הערה: באפליקציות אמיתיות לרוב מנהלים את הסיסמה רק במערכת ההזדהות ולא שומרים אותה במסד הנתונים כטקסט גלוי)
    private boolean isAdmin;     // משתנה בוליאני (אמת/שקר) המסמן האם המשתמש הזה הוא מנהל מערכת (Admin) או לא


    // בנאי מלא (Constructor).
    // מופעל כאשר רוצים ליצור אובייקט של משתמש חדש (למשל בזמן ההרשמה) ולאתחל את כל השדות שלו בבת אחת.
    public User(String id, String fname, String lname, String email, String phoneNumber, String password, boolean isAdmin) {
        this.id = id;                   // השמת המזהה למשתנה המחלקה (this מצביע על האובייקט הנוכחי)
        this.fname = fname;             // השמת השם הפרטי
        this.lname = lname;             // השמת שם המשפחה
        this.email = email;             // השמת האימייל
        this.phoneNumber = phoneNumber; // השמת מספר הטלפון
        this.password = password;       // השמת הסיסמה
        this.isAdmin = isAdmin;         // השמת הרשאות המנהל
    }

    // בנאי ריק (Empty Constructor).
    // חובה לייצר בנאי כזה כשעובדים עם מסדי נתונים בענן (כמו Firebase או Firestore), כדי שהמערכת תוכל ליצור אובייקט "ריק" וליצוק אליו את הנתונים שנמשכים מהרשת.
    public User() {
    }

    // --- מתודות Getters ו-Setters לקריאה ועדכון של נתוני המשתמש ---

    // מתודה המחזירה את השם הפרטי של המשתמש
    public String getFname() {
        return fname;
    }

    // מתודה המעדכנת את השם הפרטי של המשתמש
    public void setFname(String fname) {
        this.fname = fname;
    }

    // מתודה המחזירה את שם המשפחה
    public String getLname() {
        return lname;
    }

    // מתודה המעדכנת את שם המשפחה
    public void setLname(String lname) {
        this.lname = lname;
    }

    // מתודה המחזירה את כתובת האימייל של המשתמש
    public String getEmail() {
        return email;
    }

    // מתודה המעדכנת את האימייל של המשתמש
    public void setEmail(String email) {
        this.email = email;
    }

    // מתודה המחזירה את מספר הטלפון
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // מתודה המעדכנת את מספר הטלפון
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // מתודה המחזירה את הסיסמה
    public String getPassword() {
        return password;
    }

    // מתודה המעדכנת את הסיסמה
    public void setPassword(String password) {
        this.password = password;
    }

    // מתודה המחזירה את המזהה (ID) של המשתמש
    public String getId() {
        return id;
    }

    // מתודה המעדכנת את המזהה (ID) של המשתמש
    public void setId(String id) {
        this.id = id;
    }

    // מתודה המחזירה true אם המשתמש הוא מנהל, ו-false אם הוא משתמש רגיל
    public boolean isAdmin() {
        return isAdmin;
    }

    // מתודה המעדכנת את הרשאות הניהול של המשתמש
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // דריסה (Override) של המתודה toString.
    // מתודה זו משמשת לאריזת כל הנתונים של המשתמש לתוך מחרוזת טקסט אחת ברורה וקריאה.
    // נועד בעיקר לצרכי פיתוח ובדיקות (למשל הדפסת המשתמש ל-Logcat כדי לוודא שכל הנתונים הגיעו כראוי).
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}