import React, {useState} from 'react';
import API_BASE from './config';

export default function SignUp({onSuccess}){
  const [email,setEmail]=useState(''); 
  const [password,setPassword]=useState(''); 
  const [confirm,setConfirm]=useState(''); 
  const [err,setErr]=useState('');

  async function submit(e){
    e.preventDefault();
    if(password!==confirm){ setErr("Passwords don't match"); return; }
    try {
      const res = await fetch(`${API_BASE}/api/auth/signup`, {
        method:'POST',
        headers:{'Content-Type':'application/json'}, 
        body:JSON.stringify({email,password})
      });
      if(res.ok){ onSuccess(); }
      else { const j=await res.json(); setErr(j.message || 'Signup failed'); }
    } catch(e){
      setErr("Server error: "+e.message);
    }
  }

  return (
    <form onSubmit={submit} style={{maxWidth:400}}>
      <h3>Sign Up</h3>
      <div className="mb-3"><label>Email</label>
        <input type="email" className="form-control" required value={email} onChange={e=>setEmail(e.target.value)} />
      </div>
      <div className="mb-3"><label>Password</label>
        <input type="password" className="form-control" required value={password} onChange={e=>setPassword(e.target.value)} />
      </div>
      <div className="mb-3"><label>Confirm</label>
        <input type="password" className="form-control" required value={confirm} onChange={e=>setConfirm(e.target.value)} />
      </div>
      {err && <div className="alert alert-danger">{err}</div>}
      <button className="btn btn-success">Create Account</button>
    </form>
  );
}
