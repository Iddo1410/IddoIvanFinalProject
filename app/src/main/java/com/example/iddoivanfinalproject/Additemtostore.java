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
import android.widget.ImageButton; // ייבוא מחלקת כפתור שהוא תמונה (כרגע לא בשימוש בקובץ זה)
import android.widget.ImageView; // ייבוא מחלקה לרכיב המציג תמונה על המסך
import android.widget.Spinner; // ייבוא מחלקה לרכיב של רשימה נפתחת (תפריט גלילה)

import androidx.activity.result.ActivityResultLauncher; // ייבוא מחלקה לטיפול בתוצאות שחוזרות מפעילויות (כמו צילום תמונה)
import androidx.activity.result.contract.ActivityResultContracts; // ייבוא חוזים למערכת הפעילויות (מגדיר איזה סוג תוצאה אנו מחפשים)
import androidx.appcompat.app.AppCompatActivity; // ייבוא מחלקת הבסיס למסכים (פעילויות) באנדרואיד

import android.view.View; // ייבוא מחלקה המייצגת את אבן הבניין הבסיסית לכל אלמנט ויזואלי במסך
import android.widget.Toast; // ייבוא מחלקה להצגת הודעות פופ-אפ קצרות שמופיעות ונעלמות

import com.example.iddoivanfinalproject.model.Item; // ייבוא מחלקת המודל 'פריט' (Item) שיצרת
import com.example.iddoivanfinalproject.services.DataBaseService; // ייבוא שירות מסד הנתונים של האפליקציה (Firebase)
import com.example.iddoivanfinalproject.utils.ImageUtil; // ייבוא מחלקת עזר שיצרת לטיפול בפעולות על תמונות

public class Additemtostore extends AppCompatActivity { // הגדרת מחלקת המסך "הוספת פריט לחנות", יורשת תכונות של מסך אנדרואיד

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
        databaseService= DataBaseService.DatabaseService.getInstance(); // קבלת המופע (Instance) של שירות מסד הנתונים כדי לעבוד איתו

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

        btnAddItem.setOnClickListener(new View.OnClickListener() { // הגדרת מאזין לחיצה עבור כפתור "הוספת המוצר לחנות"
            @Override
            public void onClick(View v) { // הקוד שמתבצע בלחיצה על "הוסף"
                String itemName = etItemName.getText().toString(); // קבלת הטקסט (שם המוצר) מהשדה והמרתו למחרוזת
                String itemType=spType.getSelectedItem().toString(); // קבלת הערך שנבחר בספינר הסוג
                String itemBrand = spBrand.getSelectedItem().toString(); // קבלת הערך שנבחר בספינר המותג
                String itemPrice = etItemPrice.getText().toString(); // קבלת המחיר שהוקלד כטקסט
                String itemYear=spYear.getSelectedItem().toString(); // קבלת הערך שנבחר בספינר השנה
                String itemDetails=etItemDetails.getText().toString(); // קבלת פרטי המוצר מהשדה

                String imageBase64 = ImageUtil.convertTo64Base(imageView); // שימוש במחלקת העזר כדי להפוך את התמונה שבמסך לטקסט מסוג Base64 (כדי לשמור במסד הנתונים)
                double price = Double.parseDouble(itemPrice); // המרת מחרוזת המחיר למספר עשרוני ממשי (Double)

                // בדיקת תקינות: מוודא שאף אחד מהשדות אינו ריק
                if (itemName.isEmpty() || itemType.isEmpty() || itemBrand.isEmpty() ||
                        itemPrice.isEmpty() || itemYear.isEmpty()||itemDetails.isEmpty() ) {
                    Toast.makeText(Additemtostore.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show(); // אם חסר משהו, קופצת הודעת שגיאה
                } else {
                    Toast.makeText(Additemtostore.this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show(); // (הערה: ההודעה מוצגת פה לפני ששמרנו, אך מבחינת תקינות קלט הכל בסדר)
                }

                /// generate a new id for the item
                String id=databaseService.generateItemId(); // יצירת מזהה (ID) חדש וייחודי לפריט בעזרת שירות מסד הנתונים

                // יצירת אובייקט מסוג 'פריט' ושמירת כל הנתונים שנאספו בתוכו
                Item newItem = new Item(id, itemName, itemType, itemBrand, price, itemYear, itemDetails, imageBase64);

                /// save the item to the database and get the result in the callback
                // שליחת האובייקט לפונקציה בשירות ה-Database ששומרת אותו בענן
                databaseService.createNewItem(newItem, new DataBaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) { // בלוק שרץ אם השמירה במסד הנתונים הצליחה
                        Log.d("TAG", "Item added successfully"); // מדפיס הודעת הצלחה ללוג למפתח
                        Toast.makeText(Additemtostore.this, "Item added successfully", Toast.LENGTH_SHORT).show(); // מציג הודעה למשתמש באפליקציה על הצלחה
                        /// clear the input fields after adding the item for the next item
                        Log.d("TAG", "Clearing input fields"); // מדווח ללוג שהשדות ינוקו (למרות שבפועל הקוד עובר מסך)

                        Intent intent = new Intent(Additemtostore.this, AdminPage.class); // יצירת כוונה (Intent) למעבר אל מסך מנהל
                        startActivity(intent); // ביצוע המעבר למסך המנהל
                    }

                    @Override
                    public void onFailed(Exception e) { // בלוק שרץ אם קרתה שגיאה בשמירה
                        Log.e("TAG", "Failed to add item", e); // מדפיס את השגיאה בפירוט ללוג
                        Toast.makeText(Additemtostore.this, "Failed to add food", Toast.LENGTH_SHORT).show(); // מציג הודעת כישלון למשתמש
                    }
                });
            }
        });

        Button btnUniversalBack = findViewById(R.id.btnUniversalBack); // חיפוש וקשירת הכפתור המאפשר "חזרה אחורה" (שימי לב שכתוב במקור: "מקשר את הכפתור")
        btnUniversalBack.setOnClickListener(new View.OnClickListener() { // מאזין ללחיצה על כפתור החזרה
            @Override
            public void onClick(View v) {
                finish(); // הפקודה שסוגרת את המסך וחוזרת אחורה (בדיוק כפי שהערת לעצמך בקוד המקורי)
            }
        });

    }

    private void InitViews() { // פונקציה שעושה סדר ומקשרת את המשתנים לרכיבי הממשק ב-XML דרך ה-ID שלהם
        etItemName = findViewById(R.id.etName);
        etItemPrice = findViewById(R.id.etPrice);
        etItemDetails=findViewById(R.id.etDetails);
        spType = findViewById(R.id.spType);
        spBrand = findViewById(R.id.spBrand);
        spYear=findViewById(R.id.spYear);
        btnGallery = findViewById(R.id.btnGallery);
        btnTakePic = findViewById(R.id.btnCamera);
        btnAddItem = findViewById(R.id.btnAdd);
        imageView = findViewById(R.id.myImg);
    }

    /// select image from gallery
    private void selectImageFromGallery() { // הפונקציה שמתמודדת עם בחירת תמונה מהגלריה
        //   Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // קוד מוער (לא פעיל)
        //  selectImageLauncher.launch(intent); // קוד מוער (לא פעיל)

        imageChooser(); // הפעלה של פונקציית העזר לפתיחת תפריט בחירת הקבצים
    }

    /// capture image from camera
    private void captureImageFromCamera() { // הפונקציה שמפעילה את בקשת הצילום
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // מגדיר שאנחנו רוצים לפתוח אפליקציית מצלמה לצילום תמונה
        captureImageLauncher.launch(takePictureIntent); // משגר את הבקשה וממתין לתוצאה שתטופל למעלה (ב-captureImageLauncher)
    }

    void imageChooser() { // פונקציה לפתיחת תפריט בחירת קובץ מהמכשיר

        // create an instance of the
        // intent of the type image
        Intent i = new Intent(); // יצירת פעולה חדשה
        i.setType("image/*"); // פילטור - אנו דורשים מהמערכת להציג רק תמונות מכל סוג
        i.setAction(Intent.ACTION_GET_CONTENT); // פעולה המבקשת לקבל תוכן מתוך המכשיר (פותח גלריה או סייר קבצים)

        // pass the constant to compare it
        // with the returned requestCode
        // פותח את חלון הבחירה (Chooser) למשתמש, וכדי לדעת מאיפה חזרנו אנחנו משתמשים בקבוע SELECT_PICTURE (המספר 200)
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) { // פונקציית מערכת המופעלת ברגע שפעילות (כמו בחירת קובץ) מסתיימת
        super.onActivityResult(requestCode, resultCode, data); // מריץ את קוד האם (למקרה שיש למערכת עבודה משלה)

        if (resultCode == RESULT_OK) { // בודק האם תהליך בחירת הקובץ הצליח (המשתמש בחר תמונה ולא לחץ על 'ביטול')

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) { // בודק אם הפעילות שהסתיימה היא אכן בקשת פתיחת הגלריה שביקשנו (קוד 200)
                // Get the url of the image from data
                Uri selectedImageUri = data.getData(); // מחלץ את הכתובת (URI - נתיב) של התמונה בתוך המכשיר מהמידע שחזר
                if (null != selectedImageUri) { // אם הכתובת שהתקבלה היא לא ריקה (התקבלה בהצלחה)
                    // update the preview image in the layout
                    imageView.setImageURI(selectedImageUri); // מציב את התמונה שנבחרה ברכיב תצוגת התמונה באפליקציה שלך
                }
            }
        }
    }
}