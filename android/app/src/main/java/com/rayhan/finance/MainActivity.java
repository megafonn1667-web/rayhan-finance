package com.rayhan.finance;

import android.app.Activity;
import android.os.Bundle;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DB db;
    LinearLayout root, content;
    TextView balance, income, expense;
    final String[] incomeCats={"Наличные","Карта","Переводы","Доставка"};
    final String[] expenseCats={"Продукты","Зарплата","Аренда","Коммунальные услуги","Хозяйственные расходы","Закупки","Прочее"};
    final String[] payments={"Наличные","Карта","Перевод"};
    final int GREEN=Color.rgb(11,107,79);

    @Override public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); showHome();}
    TextView tv(String s,int sp){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(Color.rgb(30,30,30));t.setPadding(16,12,16,12);return t;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    LinearLayout.LayoutParams lp(){return new LinearLayout.LayoutParams(-1,-2);}

    void base(String title){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(248,250,249));
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(8,8,8,8);bar.setBackgroundColor(GREEN);
        TextView h=tv(title,20);h.setTextColor(Color.WHITE);h.setTypeface(null,1);bar.addView(h,new LinearLayout.LayoutParams(0,-2,1));
        Button home=btn("Главная");home.setTextColor(Color.WHITE);home.setBackgroundColor(Color.TRANSPARENT);home.setOnClickListener(v->showHome());bar.addView(home);
        root.addView(bar);
        ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(10,10,10,24);sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setPadding(2,2,2,2);
        Button a=btn("➕ Операция"),j=btn("📋 Журнал"),r=btn("📊 Отчёт");nav.addView(a,new LinearLayout.LayoutParams(0,-2,1));nav.addView(j,new LinearLayout.LayoutParams(0,-2,1));nav.addView(r,new LinearLayout.LayoutParams(0,-2,1));
        a.setOnClickListener(v->showAdd());j.setOnClickListener(v->showJournal());r.setOnClickListener(v->showReport());root.addView(nav);setContentView(root);
    }

    void showHome(){base("РАЙХАН — Отчётность");content.addView(tv("Сегодня: "+new SimpleDateFormat("dd.MM.yyyy").format(new Date()),16));balance=tv("",22);income=tv("",18);expense=tv("",18);content.addView(balance);content.addView(income);content.addView(expense);loadTotals();Button b=btn("+ Внести операцию");content.addView(b);b.setOnClickListener(v->showAdd());content.addView(tv("Данные сохраняются на телефоне и доступны без интернета.",14));}
    void loadTotals(){double inc=db.sum("Доход"),exp=db.sum("Расход");balance.setText(String.format(Locale.US,"Остаток: %.2f ₽",inc-exp));income.setText(String.format(Locale.US,"Доход: %.2f ₽",inc));expense.setText(String.format(Locale.US,"Расход: %.2f ₽",exp));}

    void showAdd(){
        base("Новая операция");
        Spinner type=new Spinner(this),cat=new Spinner(this),pay=new Spinner(this);
        EditText amount=new EditText(this);amount.setHint("Сумма, ₽");amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText person=new EditText(this);person.setHint("Ответственный");EditText note=new EditText(this);note.setHint("Комментарий");
        type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Доход","Расход"}));
        cat.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,incomeCats));pay.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,payments));
        type.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){String[] a=pos==0?incomeCats:expenseCats;cat.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,a));}});
        content.addView(tv("Тип операции",14));content.addView(type);content.addView(tv("Категория",14));content.addView(cat);content.addView(tv("Способ оплаты",14));content.addView(pay);content.addView(amount);content.addView(person);content.addView(note);
        Button save=btn("СОХРАНИТЬ");content.addView(save);save.setOnClickListener(v->{try{double a=Double.parseDouble(amount.getText().toString().replace(',','.'));if(a<=0)throw new Exception();db.add(type.getSelectedItem().toString(),cat.getSelectedItem().toString(),pay.getSelectedItem().toString(),a,person.getText().toString(),note.getText().toString());Toast.makeText(this,"Операция сохранена",Toast.LENGTH_SHORT).show();showHome();}catch(Exception e){Toast.makeText(this,"Введите корректную сумму",Toast.LENGTH_SHORT).show();}});
    }

    void showJournal(){base("Журнал операций");Cursor c=db.all();double inc=0,exp=0;while(c.moveToNext()){double a=c.getDouble(5);if(c.getString(2).equals("Доход"))inc+=a;else exp+=a;String row=c.getString(1)+"\n"+c.getString(2)+" • "+c.getString(3)+" • "+String.format(Locale.US,"%.2f ₽",a)+"\n"+c.getString(6)+"  "+c.getString(7);content.addView(tv(row,15));View line=new View(this);line.setBackgroundColor(Color.LTGRAY);content.addView(line,new LinearLayout.LayoutParams(-1,1));}c.close();content.addView(tv(String.format(Locale.US,"\nИтого доход: %.2f ₽\nИтого расход: %.2f ₽\nРезультат: %.2f ₽",inc,exp,inc-exp),17));}
    void showReport(){base("Отчёт за месяц");String m=new SimpleDateFormat("yyyy-MM").format(new Date());Cursor c=db.month(m);double inc=0,exp=0;while(c.moveToNext()){double a=c.getDouble(1);String t=c.getString(2);if(t.equals("Доход"))inc+=a;else exp+=a;content.addView(tv(c.getString(0)+"  •  "+t+"  •  "+String.format(Locale.US,"%.2f ₽",a),15));}c.close();content.addView(tv(String.format(Locale.US,"\nДоход: %.2f ₽\nРасход: %.2f ₽\nПрибыль: %.2f ₽",inc,exp,inc-exp),19));}

    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"rayhan.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE ops(id INTEGER PRIMARY KEY AUTOINCREMENT,dt TEXT,type TEXT,cat TEXT,pay TEXT,amount REAL,person TEXT,note TEXT)");}
        public void onUpgrade(SQLiteDatabase d,int a,int b){}
        void add(String t,String c,String p,double a,String person,String note){getWritableDatabase().execSQL("INSERT INTO ops(dt,type,cat,pay,amount,person,note) VALUES(?,?,?,?,?,?,?)",new Object[]{new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),t,c,p,a,person,note});}
        double sum(String t){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM ops WHERE type=?",new String[]{t});c.moveToFirst();double x=c.getDouble(0);c.close();return x;}
        Cursor all(){return getReadableDatabase().rawQuery("SELECT id,dt,type,cat,pay,amount,person,note FROM ops ORDER BY id DESC",null);}
        Cursor month(String m){return getReadableDatabase().rawQuery("SELECT substr(dt,1,10),amount,type FROM ops WHERE substr(dt,1,7)=? ORDER BY dt",new String[]{m});}
    }
}
