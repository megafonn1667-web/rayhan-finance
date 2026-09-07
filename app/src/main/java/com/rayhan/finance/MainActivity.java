package com.rayhan.finance;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    Db db; LinearLayout root, content; TextView title; String currentTab="Главная";
    final String[] incomeCats={"Наличные","Карта","Переводы","Доставка"};
    final String[] expenseCats={"Продукты","Зарплата","Аренда","Коммунальные услуги","Хозяйственные расходы","Закупки","Прочее"};
    int green=Color.rgb(14,90,69), dark=Color.rgb(7,61,48), bg=Color.rgb(246,243,236), text=Color.rgb(30,39,35), muted=Color.rgb(111,119,115), red=Color.rgb(182,66,66);

    @Override public void onCreate(Bundle b){super.onCreate(b); db=new Db(this); build(); showHome();}

    void build(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setPadding(22,22,18,18); top.setGravity(Gravity.CENTER_VERTICAL); top.setBackgroundColor(dark);
        title=new TextView(this); title.setText("РАЙХАН\nОтчётность"); title.setTextColor(Color.WHITE); title.setTextSize(23); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView plus=button("＋",Color.WHITE,dark); plus.setTextSize(30); plus.setOnClickListener(v->showAdd()); top.addView(plus,new LinearLayout.LayoutParams(56,56)); root.addView(top);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(18,18,18,12); ScrollView sv=new ScrollView(this); sv.addView(content); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this); nav.setPadding(8,7,8,7); nav.setBackgroundColor(Color.WHITE);
        String[] tabs={"Главная","Операции","Отчёты"}; for(String t:tabs){TextView x=button(t,green,Color.WHITE); x.setTextSize(13); x.setOnClickListener(v->{currentTab=t;if(t.equals("Главная"))showHome();else if(t.equals("Операции"))showOperations();else showReports();}); nav.addView(x,new LinearLayout.LayoutParams(0,55,1));}
        root.addView(nav); setContentView(root);
    }

    TextView button(String s,int fg,int bgc){TextView v=new TextView(this); v.setText(s); v.setGravity(Gravity.CENTER); v.setTextColor(fg); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setBackgroundColor(bgc); return v;}
    TextView label(String s){TextView v=new TextView(this); v.setText(s); v.setTextColor(muted); v.setTextSize(13); v.setPadding(2,8,2,4); return v;}
    TextView h(String s){TextView v=new TextView(this); v.setText(s); v.setTextColor(text); v.setTextSize(22); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setPadding(0,4,0,12); return v;}
    TextView card(String s,float size,int color){TextView v=new TextView(this); v.setText(s); v.setTextColor(color); v.setTextSize(size); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setPadding(10,20,10,20); v.setBackgroundColor(Color.WHITE); return v;}
    void add(View v){content.addView(v,new LinearLayout.LayoutParams(-1,-2));}
    void gap(int dp){Space sp=new Space(this); sp.setLayoutParams(new LinearLayout.LayoutParams(1,dp)); content.addView(sp);}
    String today(){return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());}
    double total(String type){Cursor c=db.q("select coalesce(sum(amount),0) from operations where type=? and operation_date=?",new String[]{type,today()}); double x=c.moveToFirst()?c.getDouble(0):0;c.close();return x;}
    String rub(double x){return String.format(Locale.getDefault(),"%,.2f ₽",x).replace(',',' ');}

    void showHome(){content.removeAllViews(); add(h("Сегодня, "+new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date()))); double in=total("Доход"), out=total("Расход");
        LinearLayout row=new LinearLayout(this); TextView a=card("Доход\n"+rub(in),17,green), b=card("Расход\n"+rub(out),17,red); row.addView(a,new LinearLayout.LayoutParams(0,110,1)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,110,1);bp.leftMargin=10;row.addView(b,bp);add(row);
        gap(12); add(card("Остаток за день\n"+rub(in-out),20,text)); gap(16); add(h("Быстрые действия"));
        TextView bi=button("＋  Внести доход",Color.WHITE,green); bi.setTextSize(17); bi.setOnClickListener(v->showAdd()); add(bi); gap(8);
        TextView bo=button("－  Внести расход",Color.WHITE,red); bo.setTextSize(17); bo.setOnClickListener(v->showAdd()); add(bo); gap(16);
        add(label("Последние операции")); loadList(8);
    }

    void showAdd(){ final Dialog d=new Dialog(this); d.setTitle("Новая операция"); LinearLayout l=new LinearLayout(this); l.setPadding(28,20,28,20);l.setOrientation(LinearLayout.VERTICAL);
        Spinner type=new Spinner(this); type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Доход","Расход"})); Spinner cat=new Spinner(this); EditText amount=field("Сумма, ₽",true); EditText date=field("Дата ГГГГ-ММ-ДД",false); date.setText(today()); EditText person=field("Ответственный",false); EditText note=field("Комментарий",false);
        l.addView(label("Тип"));l.addView(type);l.addView(label("Категория"));l.addView(cat);l.addView(amount);l.addView(date);l.addView(person);l.addView(note);
        Runnable sync=()->{String[] vals=type.getSelectedItem().toString().equals("Доход")?incomeCats:expenseCats;cat.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,vals));}; type.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long x){sync.run();}});sync.run();
        Button save=new Button(this);save.setText("Сохранить");save.setOnClickListener(v->{try{double a=Double.parseDouble(amount.getText().toString().replace(',','.'));if(a<=0)throw new Exception();db.insert(type.getSelectedItem().toString(),cat.getSelectedItem().toString(),a,date.getText().toString(),person.getText().toString(),note.getText().toString());d.dismiss();if(currentTab.equals("Главная"))showHome();else if(currentTab.equals("Операции"))showOperations();}catch(Exception e){Toast.makeText(this,"Введите корректную сумму",Toast.LENGTH_SHORT).show();}});l.addView(save);d.setContentView(l);d.show(); Window w=d.getWindow();if(w!=null)w.setLayout(-1,-2); }
    EditText field(String hint,boolean num){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(16);e.setSingleLine();if(num)e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);return e;}

    void showOperations(){content.removeAllViews();add(h("Журнал операций")); TextView add=button("＋ Новая операция",Color.WHITE,green);add.setTextSize(16);add.setOnClickListener(v->showAdd());this.add(add);gap(10); EditText search=field("Поиск: категория, сотрудник, комментарий",false);this.add(search);gap(6);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);content.addView(list,new LinearLayout.LayoutParams(-1,-2)); Runnable refresh=()->{list.removeAllViews();String q=search.getText().toString();Cursor c=db.q("select id,operation_date,type,category,amount,user_name,note from operations where category like ? or user_name like ? or note like ? order by operation_date desc,id desc",new String[]{"%"+q+"%","%"+q+"%","%"+q+"%"});while(c.moveToNext())addOpRow(list,c);c.close();}; search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(android.text.Editable e){}});refresh.run();
    }
    void addOpRow(LinearLayout list,Cursor c){long id=c.getLong(0);String type=c.getString(2),cat=c.getString(3),date=c.getString(1),person=c.getString(5),note=c.getString(6);double a=c.getDouble(4);TextView r=new TextView(this);r.setText((type.equals("Доход")?"＋ ":"－ ")+cat+"  •  "+date+"\n"+rub(a)+"  •  "+person+(note.isEmpty()?"":"\n"+note));r.setTextSize(15);r.setTextColor(type.equals("Доход")?green:red);r.setPadding(14,15,10,15);r.setBackgroundColor(Color.WHITE);r.setOnClickListener(v->editDelete(id));list.addView(r,new LinearLayout.LayoutParams(-1,-2));Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(1,6));list.addView(s);}
    void editDelete(long id){new AlertDialog.Builder(this).setTitle("Операция").setItems(new String[]{"Удалить"},(d,w)->{if(w==0){db.delete(id);showOperations();}}).show();}
    void loadList(int limit){Cursor c=db.q("select operation_date,type,category,amount,user_name from operations where operation_date=? order by id desc limit "+limit,new String[]{today()});while(c.moveToNext()){TextView x=new TextView(this);x.setText((c.getString(1).equals("Доход")?"＋ ":"－ ")+c.getString(2)+" — "+rub(c.getDouble(3))+"\n"+c.getString(4));x.setTextSize(15);x.setTextColor(text);x.setPadding(13,13,8,13);x.setBackgroundColor(Color.WHITE);add(x);gap(5);}c.close();}

    void showReports(){content.removeAllViews();add(h("Отчёты"));String month=new SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(new Date());add(label("Текущий месяц: "+month)); Cursor c=db.q("select operation_date, sum(case when type='Доход' then amount else 0 end), sum(case when type='Расход' then amount else 0 end) from operations where substr(operation_date,1,7)=? group by operation_date order by operation_date",new String[]{month});double ti=0,to=0;while(c.moveToNext()){double i=c.getDouble(1),o=c.getDouble(2);ti+=i;to+=o;TextView r=card(c.getString(0)+"\nДоход: "+rub(i)+"    Расход: "+rub(o)+"\nИтог: "+rub(i-o),15,text);add(r);gap(6);}c.close();gap(8);add(card("ИТОГО ЗА МЕСЯЦ\nДоход: "+rub(ti)+"\nРасход: "+rub(to)+"\nПРИБЫЛЬ: "+rub(ti-to),19,green));}

    static class Db extends SQLiteOpenHelper{
        Db(Context c){super(c,"rayhan.db",null,1);} public void onCreate(SQLiteDatabase x){x.execSQL("create table operations(id integer primary key autoincrement, operation_date text not null, type text not null, category text not null, amount real not null, user_name text, note text, created_at text)");}
        public void onUpgrade(SQLiteDatabase x,int a,int b){}
        void insert(String type,String cat,double amount,String date,String user,String note){ContentValues v=new ContentValues();v.put("operation_date",date);v.put("type",type);v.put("category",cat);v.put("amount",amount);v.put("user_name",user);v.put("note",note);v.put("created_at",System.currentTimeMillis());getWritableDatabase().insert("operations",null,v);}
        Cursor q(String s,String[] a){return getReadableDatabase().rawQuery(s,a);} void delete(long id){getWritableDatabase().delete("operations","id=?",new String[]{String.valueOf(id)});}
    }
}
