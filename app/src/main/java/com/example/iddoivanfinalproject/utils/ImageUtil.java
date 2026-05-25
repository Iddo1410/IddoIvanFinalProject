// הגדרת החבילה (Package) שבה נמצא הקובץ, בתוך תיקיית הכלים (utils) של הפרויקט
package com.example.iddoivanfinalproject.utils;

// ייבוא מחלקות וספריות של אנדרואיד וג'אווה לטיפול בהרשאות, תמונות (Bitmap) והמרות קבצים (Base64)
import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

// מחלקת עזר (Utility) לטיפול בפעולות הקשורות לתמונות.
// המחלקה מכילה פונקציות עזר כלליות שניתן לקרוא להן מכל מקום באפליקציה (הן מוגדרות כ-static).
public class ImageUtil {

    // --- מתודה לבקשת הרשאות (Permissions) ---
    // פונקציה זו מבקשת מהמשתמש לאשר לאפליקציה גישה למצלמה ולאחסון המכשיר.
    // הפונקציה מקבלת את ה-Activity (המסך) שממנו מתבצעת הבקשה.
    public static void requestPermission(@NotNull Activity activity) {
        // הפעלת חלון הבקשה המובנה של אנדרואיד עם מערך של 3 הרשאות:
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.CAMERA,                  // הרשאת גישה למצלמה
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,  // הרשאת כתיבה (שמירה) באחסון החיצוני
                        Manifest.permission.READ_EXTERNAL_STORAGE    // הרשאת קריאה מהאחסון החיצוני
                }, 1); // המספר 1 הוא "קוד הבקשה" (Request Code) כדי שנוכל לזהות את התשובה של המשתמש אחר כך
    }

    // --- מתודה להמרת תמונה למחרוזת טקסט (Base64) ---
    // פונקציה זו לוקחת תמונה שמוצגת ברכיב ImageView, וממירה אותה לטקסט ארוך.
    // זה הכרחי כדי שנוכל לשמור תמונות במסד נתונים של טקסט (כמו Firebase Realtime Database).
    public static @Nullable String convertTo64Base(@NotNull final ImageView postImage) {
        // בדיקה: אם אין תמונה בתוך ה-ImageView, החזר null (כלומר, אין מה להמיר)
        if (postImage.getDrawable() == null) {
            return null;
        }

        // חילוץ אובייקט התמונה (Bitmap) מתוך הרכיב הגרפי (ImageView)
        Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();

        // יצירת "צינור פלט" (Stream) שלתוכו נדחס את המידע של התמונה
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // דחיסת התמונה לפורמט JPEG באיכות מקסימלית (100%) לתוך הצינור שיצרנו
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        // הפיכת הנתונים שנדחסו למערך של בתים (Byte Array)
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // קידוד מערך הבתים לפורמט Base64 (שהוא בעצם מחרוזת טקסט ארוכה) והחזרת המחרוזת
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // --- מתודה להמרת מחרוזת טקסט (Base64) חזרה לתמונה ---
    // פונקציה זו עושה את הפעולה ההפוכה: מקבלת מחרוזת טקסט (למשל כזו שנשלפה מ-Firebase) והופכת אותה חזרה לתמונה.
    public static @Nullable Bitmap convertFrom64base(@NotNull final String base64Code) {
        // בדיקה: אם מחרוזת הטקסט ריקה, החזר null (אין מה להמיר)
        if (base64Code.isEmpty()) {
            return null;
        }

        // פענוח מחרוזת הטקסט (Base64) חזרה למערך של בתים (Bytes)
        byte[] decodedString = Base64.decode(base64Code, Base64.DEFAULT);

        // שימוש במחלקת BitmapFactory של אנדרואיד כדי להפוך את מערך הבתים לאובייקט של תמונה (Bitmap) שניתן להציג על המסך
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}