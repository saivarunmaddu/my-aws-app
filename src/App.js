import React, {useState} from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import SignIn from './SignIn';
import SignUp from './SignUp';
import Gallery from './Gallery';

export default function App(){
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [view, setView] = useState('signin'); // or 'signup' or 'gallery'

  return (
    <div className="container mt-5">
      <div className="mb-3">
        <button className="btn btn-primary me-2" onClick={()=>setView('signin')}>Sign In</button>
        <button className="btn btn-secondary" onClick={()=>setView('signup')}>Sign Up</button>
      </div>

      { token && view === 'signin' && <Gallery token={token} onLogout={()=>{
           localStorage.removeItem('token'); setToken(null); setView('signin');
      }} /> }

      { !token && view==='signin' && <SignIn onSuccess={(t)=>{ localStorage.setItem('token', t); setToken(t); setView('gallery'); }} /> }
      { !token && view==='signup' && <SignUp onSuccess={()=>setView('signin')} /> }
    </div>
  );
}
