// הגדרת החבילה (Package) שבה נמצא הקובץ, תחת תיקיית המודלים (model)
package com.example.iddoivanfinalproject.model;

// ייבוא ממשק List מתוך ספריית הכלים של Java, המאפשר שימוש ברשימות נתונים
import java.util.List;

// הגדרת המחלקה Order, המייצגת הזמנה שלמה שבוצעה על ידי משתמש
public class Order {

    // --- מאפייני ההזמנה (המשתנים) ---
    // מוגדרים כ-private כדי למנוע גישה ישירה אליהם מחוץ למחלקה (עקרון הכימוס)
    private String id;            // מזהה ייחודי (ID) של ההזמנה עצמה במסד הנתונים
    private String userId;        // מזהה ייחודי (ID) של המשתמש שביצע את ההזמנה
    private String userEmail;     // כתובת האימייל של המשתמש שביצע את ההזמנה
    private List<Cart> items;     // רשימה של כל הפריטים (אובייקטים מסוג Cart) שנכללו בהזמנה
    private double totalPrice;    // הסכום הכולל לתשלום עבור כל ההזמנה
    private long timestamp;       // חותמת זמן (נשמרת כמספר ארוך של מילישניות) המייצגת מתי בוצעה ההזמנה

    // בנאי ריק (Empty Constructor).
    // חובה ליצור בנאי כזה כשעובדים עם מסדי נתונים כמו Firebase, כדי שהמערכת תוכל ליצור אובייקט ריק ולמשוך אליו את הנתונים מהשרת.
    public Order() {}

    // בנאי (Constructor) עם פרמטרים.
    // מופעל כשרוצים ליצור הזמנה חדשה באפליקציה (למשל, כשהמשתמש לוחץ על "שלם" בעגלת הקניות).
    // שים לב שהמזהה (id) של ההזמנה לא מועבר לכאן, כי בדרך כלל הוא נוצר אוטומטית על ידי מסד הנתונים.
    public Order(String userId, String userEmail, List<Cart> items, double totalPrice, long timestamp) {
        this.userId = userId;         // השמת מזהה המשתמש שביצע את ההזמנה
        this.userEmail = userEmail;   // השמת האימייל של המשתמש
        this.items = items;           // השמת רשימת הפריטים (העגלה שאושרה)
        this.totalPrice = totalPrice; // השמת המחיר הכולל לתשלום
        this.timestamp = timestamp;   // השמת הזמן המדויק בו בוצעה ההזמנה
    }

    // --- מתודות Getters ו-Setters המאפשרות לקבל ולעדכן את הנתונים ---

    // מתודה המחזירה את מזהה ההזמנה
    public String getId() { return id; }

    // מתודה המעדכנת את מזהה ההזמנה (לרוב מופעלת לאחר שמסד הנתונים יצר את ההזמנה והחזיר את ה-ID שלה)
    public void setId(String id) { this.id = id; }

    // מתודה המחזירה את מזהה המשתמש (הקונה)
    public String getUserId() { return userId; }

    // מתודה המחזירה את אימייל המשתמש
    public String getUserEmail() { return userEmail; }

    // מתודה המחזירה את רשימת הפריטים שנקנו בהזמנה זו
    public List<Cart> getItems() { return items; }

    // מתודה המחזירה את הסכום הכולל של ההזמנה
    public double getTotalPrice() { return totalPrice; }

    // מתודה המחזירה את זמן ביצוע ההזמנה (במילישניות)
    public long getTimestamp() { return timestamp; }

    // דריסה (Override) של המתודה toString.
    // מתודה זו מחזירה מחרוזת טקסט ארוכה שמכילה את כל פרטי ההזמנה.
    // שימושי מאוד במיוחד לצורכי דיבאגינג, למשל אם תרצה להדפיס את ההזמנה בחלון ה-Logcat כדי לבדוק שהנתונים נשמרו נכון.
    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", items=" + items +
                ", totalPrice=" + totalPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}