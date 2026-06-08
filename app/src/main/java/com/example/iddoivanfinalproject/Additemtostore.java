package com.example.iddoivanfinalproject; // הגדרת חבילת הפרויקט (הנתיב בו נמצא הקובץ במערכת)

import android.content.Intent; // ייבוא מחלקה המאפשרת מעבר בין מסכים באפליקציה או הפעלת פעולות חיצוניות
import android.graphics.Bitmap; // ייבוא מחלקה לטיפול וניהול תמונות מבוססות פיקסלים
import android.net.Uri; // ייבוא מחלקה המייצגת כתובות משאבים (למשל נתיב לתמונה בגלריה)
import android.os.Bundle; // ייבוא מחלקה ששומרת נתונים על מצב המסך בזמן פתיחתו או שחזורו
import android.provider.MediaStore; // ייבוא מחלקה המספקת גישה למדיה של המכשיר (מצלמה, תמונות וכו')
import android.util.Log; // ייבוא מחלקה המאפשרת הדפסת הודעות למסוף (לוג) לצורכי דיבאג ובקרה
import android.widget.ArrayAdapter; // ייבוא מחלקה שתפקידה לקחת נתונים (כמו מערך) ולהציג אותם ברשימה (כמו ספינר)
import android.widget.Button; // ייבוא מחלקת רכיב הכפתור
import android.widget.EditText; // ייבוא מחלקה לרכיב של שדה טקסט שניתן לעריכה על ידי המשתמש
import android.widget.ImageView; // ייבוא מחלקה לרכיב המציג תמונה על המסך
import android.widget.Spinner; // ייבוא מחלקה לרכיב של רשימה נפתחת (תפריט גלילה)
import android.view.View; // ייבוא מחלקה המייצגת את אבן הבניין הבסיסית לכל אלמנט ויזואלי במסך
import android.widget.Toast; // ייבוא מחלקה להצגת הודעות פופ-אפ קצרות שמופיעות ונעלמות

import androidx.activity.result.ActivityResultLauncher; // ייבוא מחלקה לטיפול בתוצאות שחוזרות מפעילויות (כמו צילום תמונה)
import androidx.activity.result.contract.ActivityResultContracts; // ייבוא חוזים למערכת הפעילויות (מגדיר איזה סוג תוצאה אנו מחפשים)
import androidx.appcompat.app.AppCompatActivity; // ייבוא מחלקת הבסיס למסכים (פעילויות) באנדרואיד

import com.example.iddoivanfinalproject.model.Item; // ייבוא מחלקת המודל 'פריט' (Item) שיצרת
import com.example.iddoivanfinalproject.services.DataBaseService; // ייבוא שירות מסד הנתונים של האפליקציה (Firebase)
import com.example.iddoivanfinalproject.utils.ImageUtil; // ייבוא מחלקת עזר שיצרת לטיפול בפעולות על תמונות

public class Additemtostore extends AppCompatActivity {
    // הגדרת משתנים פרטיים שייצגו את שדות הטקסט במסך
    private EditText etItemName, etItemPrice, etItemType, etItemBrand, etItemYear, etItemDetails;
    // הגדרת משתנים פרטיים שייצגו את התפריטים הנפתחים
    private Spinner spType, spBrand, spYear;
    // הגדרת משתנים פרטיים שייצגו את הכפתורים במסך
    private Button btnGallery, btnTakePic, btnAddItem, btnBack;
    // הגדרת משתנה פרטי שייצג את התמונה שתוצג במסך
    private ImageView imageView;

    // משתנה שיאפשר קריאה וגישה לפעולות שירות מסד הנתונים
    private DataBaseService.DatabaseService databaseService;

    /// Activity result launcher for capturing image from camera
    // אובייקט שישמש להפעלת המצלמה וקבלת התמונה שצולמה בחזרה
    private ActivityResultLauncher<Intent> captureImageLauncher;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200; // הגדרת קבוע מספרי שישמש לזהות מתי חזרנו מפעולת בחירת תמונה מהגלריה

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שרצה ראשונה כשהמסך נוצר
        super.onCreate(savedInstanceState); // קריאה לפעולת ה-onCreate של מחלקת האם כדי להגדיר את המסך
        setContentView(R.layout.activity_additemtostore); // קביעת קובץ העיצוב (ה-XML) שיוצג על המסך הזה

        InitViews(); // קריאה לפונקציית עזר המקשרת בין המשתנים שהגדרנו למעלה לרכיבים בקובץ ה-XML

        /// request permission for the camera and storage
        ImageUtil.requestPermission(this); // קריאה למחלקת העזר לבקש מהמשתמש הרשאות גישה למצלמה ולאחסון המכשיר

        /// get the instance of the database service
        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת המופע (Instance) של שירות מסד הנתונים כדי לעבוד איתו

        // יצירת מתווך (Adapter) שמחבר בין מערך סוגי המוצרים (מה-XML) לתפריט הנפתח
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, // המסך הנוכחי
                R.array.typeArr, // מערך הסוגים המוגדר ב-res/values
                android.R.layout.simple_spinner_item // עיצוב שורת הטקסט כשהספינר סגור
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // עיצוב השורות כשהרשימה פתוחה
        spType.setAdapter(typeAdapter); // חיבור המתווך לספינר של סוג המוצר בפועל

        // יצירת מתווך (Adapter) שמחבר בין מערך שנות הייצור לתפריט הנפתח של שנת הייצור
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.yearArr, // מערך השנים
                android.R.layout.simple_spinner_item
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter); // חיבור המתווך לספינר של השנה

        // יצירת מתווך (Adapter) שמחבר בין מערך המותגים לתפריט הנפתח של המותגים
        ArrayAdapter<CharSequence> brandAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.brandArr, // מערך המותגים
                android.R.layout.simple_spinner_item
        );
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBrand.setAdapter(brandAdapter); // חיבור המתווך לספינר של המותג

        /// register the activity result launcher for capturing image from camera
        // הגדרת רכיב ה"משגר" שלוקח את האחריות על הפעלת המצלמה וטיפול בתוצאה שלה
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // אנו מגדירים חוזה של פתיחת חלון והמתנה לתוצאה
                result -> { // בלוק הקוד שירוץ כאשר חזרנו מצילום התמונה
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) { // בדיקה שהצילום הצליח ויש נתונים
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data"); // חילוץ התמונה מתוך הנתונים במבנה Bitmap
                        imageView.setImageBitmap(bitmap); // עדכון רכיב תצוגת התמונה במסך עם התמונה שצולמה
                    }
                });

        btnGallery.setOnClickListener(new View.OnClickListener() { // הגדרת מאזין לחיצה עבור כפתור "בחירה מגלריה"
            @Override
            public void onClick(View v) { // הקוד שיתבצע כשהמשתמש ילחץ על הכפתור
                selectImageFromGallery(); // קריאה לפונקציה שפותחת את הגלריה
            }
        });


        btnTakePic.setOnClickListener(new View.OnClickListener() { // הגדרת מאזין לחיצה עבור כפתור "צילום תמונה"
            @Override
            public void onClick(View v) { // הקוד שיתבצע כשהמשתמש ילחץ על הכפתור
                captureImageFromCamera(); // קריאה לפונקציה שמפעילה את המצלמה
            }
        });

        // כפתור הוספת המוצר - עבר תיקון למניעת קריסות (Null/NumberFormat)
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // שולפים את כל הטקסטים ומנקים רווחים מיותרים עם trim()
                String itemName = etItemName.getText().toString().trim();
                String itemPrice = etItemPrice.getText().toString().trim();
                String itemDetails = etItemDetails.getText().toString().trim();

                // מוודאים שהספינרים החזירו ערך תקין ולא יגרמו לקריסה
                String itemType = spType.getSelectedItem() != null ? spType.getSelectedItem().toString() : "";
                String itemBrand = spBrand.getSelectedItem() != null ? spBrand.getSelectedItem().toString() : "";
                String itemYear = spYear.getSelectedItem() != null ? spYear.getSelectedItem().toString() : "";

                // 1. בדיקת תקינות: מוודא שאף אחד מהשדות אינו ריק
                if (itemName.isEmpty() || itemType.isEmpty() || itemBrand.isEmpty() ||
                        itemPrice.isEmpty() || itemYear.isEmpty() || itemDetails.isEmpty()) {
                    Toast.makeText(Additemtostore.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return; // קריטי! עוצר את הפעולה ולא נותן לאפליקציה לקרוס
                }

                // 2. המרת המחיר למספר (מתוך בלוק בטיחות)
                double price;
                try {
                    price = Double.parseDouble(itemPrice);
                } catch (NumberFormatException e) {
                    Toast.makeText(Additemtostore.this, "אנא הזן מחיר חוקי", Toast.LENGTH_SHORT).show();
                    return; // אם המחיר לא חוקי עוצרים את השמירה
                }

                // 3. הכל תקין! נמיר את התמונה ונשמור
                String imageBase64 = ImageUtil.convertTo64Base(imageView);
                Toast.makeText(Additemtostore.this, "מתחיל בשמירת המוצר...", Toast.LENGTH_SHORT).show();

                String id = databaseService.generateItemId(); // יצירת ID חדש
                Item newItem = new Item(id, itemName, itemType, itemBrand, price, itemYear, itemDetails, imageBase64);

                // שמירה ל-Firebase
                databaseService.createNewItem(newItem, new DataBaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Log.d("TAG", "Item added successfully");
                        Toast.makeText(Additemtostore.this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Additemtostore.this, AdminPage.class);
                        startActivity(intent);
                        finish(); // סוגר את עמוד ההוספה שלא יחזרו אליו
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e("TAG", "Failed to add item", e);
                        Toast.makeText(Additemtostore.this, "שגיאה! לא ניתן לשמור את המוצר.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btnUniversalBack = findViewById(R.id.btnUniversalBack); // חיפוש וקשירת הכפתור המאפשר "חזרה אחורה"
        btnUniversalBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // הפקודה שסוגרת את המסך וחוזרת אחורה
            }
        });

    }

    private void InitViews() { // פונקציה שעושה סדר ומקשרת את המשתנים לרכיבי הממשק ב-XML דרך ה-ID שלהם
        etItemName = findViewById(R.id.etName);
        etItemPrice = findViewById(R.id.etPrice);
        etItemDetails = findViewById(R.id.etDetails);
        spType = findViewById(R.id.spType);
        spBrand = findViewById(R.id.spBrand);
        spYear = findViewById(R.id.spYear);
        btnGallery = findViewById(R.id.btnGallery);
        btnTakePic = findViewById(R.id.btnCamera);
        btnAddItem = findViewById(R.id.btnAdd);
        imageView = findViewById(R.id.myImg);
    }

    /// select image from gallery
    private void selectImageFromGallery() { // הפונקציה שמתמודדת עם בחירת תמונה מהגלריה
        imageChooser(); // הפעלה של פונקציית העזר לפתיחת תפריט בחירת הקבצים
    }

    /// capture image from camera
    private void captureImageFromCamera() { // הפונקציה שמפעילה את בקשת הצילום
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // מגדיר שאנחנו רוצים לפתוח אפליקציית מצלמה לצילום תמונה
        captureImageLauncher.launch(takePictureIntent); // משגר את הבקשה וממתין לתוצאה שתטופל למעלה (ב-captureImageLauncher)
    }

    void imageChooser() { // פונקציה לפתיחת תפריט בחירת קובץ מהמכשיר
        Intent i = new Intent(); // יצירת פעולה חדשה
        i.setType("image/*"); // פילטור - אנו דורשים מהמערכת להציג רק תמונות מכל סוג
        i.setAction(Intent.ACTION_GET_CONTENT); // פעולה המבקשת לקבל תוכן מתוך המכשיר (פותח גלריה או סייר קבצים)
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { // בודק האם תהליך בחירת הקובץ הצליח
            if (requestCode == SELECT_PICTURE) { // מוודא שזה הקוד של בחירת התמונה שלנו
                Uri selectedImageUri = data.getData(); // מחלץ את הכתובת של התמונה
                if (null != selectedImageUri) {
                    imageView.setImageURI(selectedImageUri); // מציב את התמונה שנבחרה בתצוגה
                }
            }
        }
    }
}