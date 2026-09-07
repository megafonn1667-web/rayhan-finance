import React,{useEffect,useMemo,useState} from 'react';
import{createRoot}from'react-dom/client';
import{createClient}from'@supabase/supabase-js';
import'./style.css';

const url=import.meta.env.VITE_SUPABASE_URL;
const key=import.meta.env.VITE_SUPABASE_ANON_KEY;
const sb=createClient(url,key);
const cats={Доход:['Наличные','Карта','Переводы','Доставка'],Расход:['Продукты','Зарплата','Аренда','Коммунальные','Хозяйственные','Закупки','Прочее']};
const money=n=>Number(n||0).toLocaleString('ru-RU')+' ₽';

function Auth(){
 const[email,setEmail]=useState(''),[password,setPassword]=useState(''),[register,setRegister]=useState(false),[msg,setMsg]=useState(''),[busy,setBusy]=useState(false);
 async function submit(e){e.preventDefault();setBusy(true);setMsg('');
  const r=register?await sb.auth.signUp({email,password}):await sb.auth.signInWithPassword({email,password});
  setBusy(false);
  if(r.error)setMsg(r.error.message);else setMsg(register?'Аккаунт создан. Проверьте почту для подтверждения.':'Выполнен вход.');
 }
 return <div className="auth"><form className="authCard" onSubmit={submit}><div className="brandMark">☕</div><small>ФИНАНСОВАЯ СИСТЕМА ЧАЙХАНЫ</small><h1>РАЙХАН</h1><p>Облачная отчётность с любого устройства</p><input type="email" placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} required/><input type="password" placeholder="Пароль" value={password} onChange={e=>setPassword(e.target.value)} minLength={6} required/><button className="save">{busy?'Подождите…':register?'Создать аккаунт':'Войти'}</button>{msg&&<div className="msg">{msg}</div>}<button type="button" className="link" onClick={()=>setRegister(!register)}>{register?'У меня уже есть аккаунт':'Создать новый аккаунт'}</button></form></div>
}

function App(){
 const[session,setSession]=useState(null),[loading,setLoading]=useState(true),[items,setItems]=useState([]),[type,setType]=useState('Доход'),[cat,setCat]=useState('Наличные'),[amount,setAmount]=useState(''),[note,setNote]=useState(''),[date,setDate]=useState(new Date().toISOString().slice(0,10)),[tab,setTab]=useState('Главная'),[msg,setMsg]=useState('');
 useEffect(()=>{sb.auth.getSession().then(({data})=>{setSession(data.session);setLoading(false)});const{data:{subscription}}=sb.auth.onAuthStateChange((_e,s)=>setSession(s));return()=>subscription.unsubscribe()},[]);
 useEffect(()=>{if(session)load()},[session]);
 async function load(){const{data,error}=await sb.from('operations').select('*').eq('user_id',session.user.id).order('operation_date',{ascending:false}).order('id',{ascending:false});if(error)setMsg('Ошибка загрузки: '+error.message);else setItems(data||[])}
 function changeType(v){setType(v);setCat(cats[v][0])}
 async function add(){const a=Number(amount);if(!a||a<=0){setMsg('Введите сумму');return}const x={user_id:session.user.id,operation_date:date,type,category:cat,amount:a,note};const{error}=await sb.from('operations').insert(x);if(error){setMsg('Не удалось сохранить: '+error.message);return}setAmount('');setNote('');setMsg('Операция сохранена ✓');load()}
 async function del(id){if(!confirm('Удалить операцию?'))return;const{error}=await sb.from('operations').delete().eq('id',id).eq('user_id',session.user.id);if(error)setMsg(error.message);else load()}
 const today=new Date().toISOString().slice(0,10);const inc=items.filter(x=>x.type==='Доход'&&x.operation_date===today).reduce((s,x)=>s+Number(x.amount),0),exp=items.filter(x=>x.type==='Расход'&&x.operation_date===today).reduce((s,x)=>s+Number(x.amount),0);const totalInc=items.filter(x=>x.type==='Доход').reduce((s,x)=>s+Number(x.amount),0),totalExp=items.filter(x=>x.type==='Расход').reduce((s,x)=>s+Number(x.amount),0);
 if(loading)return <div className="loading">Загрузка…</div>;if(!session)return <Auth/>;
 return <div className="app"><aside><h1>☕ РАЙХАН</h1><small>ФИНАНСОВАЯ СИСТЕМА</small>{['Главная','Новая операция','Операции','Отчёты'].map(x=><button key={x} className={tab===x?'active':''} onClick={()=>setTab(x)}>{x}</button>)}<div className="offline">{session.user.email}<br/>Данные в облаке</div><button onClick={()=>sb.auth.signOut()}>Выйти</button></aside><main>{tab==='Главная'&&<><h2>Финансовая панель</h2><p>Сегодня, {new Date().toLocaleDateString('ru-RU')}</p><section className="cards"><Card t="Доход сегодня" v={inc}/><Card t="Расход сегодня" v={exp}/><Card t="Чистая прибыль" v={inc-exp}/><Card t="Операций" v={items.length} raw/></section><h3>Последние операции</h3><Table items={items.slice(0,8)} del={del}/></>}{tab==='Новая операция'&&<div className="form"><h2>Новая операция</h2><label>Дата<input type="date" value={date} onChange={e=>setDate(e.target.value)}/></label><label>Тип<select value={type} onChange={e=>changeType(e.target.value)}><option>Доход</option><option>Расход</option></select></label><label>Категория<select value={cat} onChange={e=>setCat(e.target.value)}>{cats[type].map(x=><option key={x}>{x}</option>)}</select></label><label>Сумма, ₽<input type="number" min="0" step="0.01" value={amount} onChange={e=>setAmount(e.target.value)} placeholder="0"/></label><label>Комментарий<input value={note} onChange={e=>setNote(e.target.value)} placeholder="Необязательно"/></label><button className="save" onClick={add}>СОХРАНИТЬ ОПЕРАЦИЮ</button>{msg&&<div className="msg">{msg}</div>}</div>}{tab==='Операции'&&<><h2>Все операции</h2><Table items={items} del={del}/></>}{tab==='Отчёты'&&<><h2>Общий отчёт</h2><section className="cards"><Card t="Все доходы" v={totalInc}/><Card t="Все расходы" v={totalExp}/><Card t="Общая прибыль" v={totalInc-totalExp}/></section><h3>По категориям</h3>{Object.entries(items.reduce((a,x)=>{a[x.category]=(a[x.category]||0)+Number(x.amount);return a},{})).map(([k,v])=><div className="line" key={k}><span>{k}</span><b>{money(v)}</b></div>)}</>}</main></div>
}
function Card({t,v,raw}){return <div className="card"><span>{t}</span><strong>{raw?v:money(v)}</strong></div>}
function Table({items,del}){return <div className="table">{items.length===0?<p>Операций пока нет.</p>:items.map(x=><div className="tr" key={x.id}><span>{x.operation_date}</span><span className={x.type==='Доход'?'income':'expense'}>{x.type}</span><span>{x.category}</span><b>{money(x.amount)}</b><button onClick={()=>del(x.id)}>×</button></div>)}</div>}
createRoot(document.getElementById('root')).render(<App/>);
