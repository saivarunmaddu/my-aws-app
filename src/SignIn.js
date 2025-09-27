import React, {useState} from 'react';
import API_BASE from './config';

export default function SignIn({onSuccess}){
  const [email,setEmail]=useState(''); 
  const [password,setPassword]=useState(''); 
  const [err,setErr]=useState('');

  async function submit(e){
    e.preventDefault();
    try {
      const res = await fetch(`${API_BASE}/api/auth/signin`, {
        method:'POST', 
        headers:{'Content-Type':'application/json'}, 
        body:JSON.stringify({email,password})
      });
      if(res.ok){ 
        const j=await res.json(); 
        onSuccess(j.token); 
      }
      else { 
        const j=await res.json(); 
        setErr(j.message || 'Login failed'); 
      }
    } catch(e){
      setErr("Server error: "+e.message);
    }
  }

  return (
    <form onSubmit={submit} style={{maxWidth:400}}>
      <h3>Sign In</h3>
      <div className="mb-3"><label>Email</label>
        <input type="email" className="form-control" required value={email} onChange={e=>setEmail(e.target.value)}/>
      </div>
      <div className="mb-3"><label>Password</label>
        <input type="password" className="form-control" required value={password} onChange={e=>setPassword(e.target.value)}/>
      </div>
      {err && <div className="alert alert-danger">{err}</div>}
      <button className="btn btn-primary">Sign In</button>
    </form>
  );
}
