package com.example.iddoivanfinalproject; // הגדרת המיקום של הקובץ בתוך התיקיות של הפרויקט

// ייבוא המחלקות הדרושות מהספריות של אנדרואיד
import android.content.Intent; // מחלקה האחראית על מעבר בין מסכים
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן פתיחתו
import android.view.View;
import android.view.animation.Animation; // מחלקה לייצוג אנימציות (כמו הופעה הדרגתית, תזוזה וכו')
import android.view.animation.AnimationUtils; // מחלקת עזר לטעינת קבצי אנימציה מתוך תיקיית המשאבים (res)
import android.widget.ImageView; // רכיב להצגת תמונה במסך

import androidx.activity.EdgeToEdge; // מחלקה לתצוגת מסך מלאה (ללא שוליים עליונים/תחתונים שחורים)
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית למסכי אנדרואיד
import androidx.core.graphics.Insets; // מחלקה לטיפול בשוליים הפנימיים
import androidx.core.view.ViewCompat; // מחלקה לטיפול בתצוגה גרפית
import androidx.core.view.WindowInsetsCompat; // מחלקה לטיפול באזורי מסך מיוחדים (שורת סטטוס וכו')

public class SplashActivity extends BaseActivity { // הגדרת המחלקה של מסך הפתיחה
    private ImageView myImageView; // משתנה שייצג את התמונה שמופיעה במסך הפתיחה הלוגו שלך)
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נוצר
        super.onCreate(savedInstanceState); // קריאה לפונקציית האם
        setContentView(R.layout.activity_splash); // חיבור קוד ה-Java הנוכחי אל קובץ ה-XML של העיצוב (activity_splash)

        // קישור המשתנה ב-Java אל רכיב התמונה שנמצא בקובץ ה-XML (לפי ה-ID שלו)
        myImageView = (ImageView)findViewById(R.id.imageView);

        // יצירת "תהליכון" (Thread) חדש.
        // תהליכון מאפשר לנו להריץ קוד ברקע מבלי לתקוע את התצוגה המרכזית של האפליקציה (כדי שהיא לא תקרוס בזמן ההמתנה)
        Thread mSplashThread = new Thread() {
            @Override
            public void run() { // הפונקציה שתרוץ בתוך התהליכון ברקע
                try {
                    synchronized (this) { // "נעילת" התהליכון כדי שנוכל להשתמש בפקודת המתנה (wait) בצורה בטוחה

                        // טעינת האנימציה שיצרת מקובץ ה-XML של האנימציות (R.anim.anim)
                        Animation myFadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.anim);

                        // 💡 הערה למפתח: טענת את האנימציה, אבל כדי שהיא תעבוד חסרה הפקודה שמפעילה אותה על התמונה!
                        // אם תרצה שהיא תפעל, הוסף את השורה הבאה מחוץ ל-Thread (ממש מתחת ל-findViewById):
                        // myImageView.startAnimation(myFadeInAnimation);

                        wait(3000); // פקודת המתנה (השהייה) של 3000 מילי-שניות (שהן בדיוק 3 שניות)
                    }
                } catch (InterruptedException ex) { // במקרה שהייתה שגיאה וההמתנה נקטעה מסיבה כלשהי
                    finish(); // סוגר את המסך באופן מיידי כדי למנוע קריסה
                }

                // לאחר שעברו 3 שניות (ה-wait הסתיים), הקוד ממשיך לכאן:
                // יצירת כוונה (Intent) לעבור ממסך הפתיחה למסך הראשי (MainActivity)
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent); // הפעלת המעבר למסך הראשי

                // מומלץ להוסיף כאן גם את הפקודה finish(); כדי לסגור את מסך הפתיחה
                // כך המשתמש לא יוכל לחזור אליו אם הוא ילחץ על כפתור "אחורה" במסך הראשי.
            }
        };

        mSplashThread.start(); // הפקודה שמפעילה בפועל את התהליכון שיצרנו למעלה (מתחילה את הספירה לאחור)
    }
}