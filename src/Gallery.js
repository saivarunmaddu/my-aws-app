import React, {useEffect, useState} from 'react';
import API_BASE from './config';

export default function Gallery({token, onLogout}){
  const [images, setImages] = useState([]);
  const [err, setErr] = useState('');

  async function loadImages(){
    try {
      const res = await fetch(`${API_BASE}/api/images`, {
        headers:{'Authorization':'Bearer '+token}
      });
      if(res.ok){
        const imgs = await res.json();
        setImages(imgs);
      }
    } catch(e){
      setErr("Failed to load images: "+e.message);
    }
  }

  useEffect(()=>{ loadImages(); },[]);

  async function upload(e){
    const file = e.target.files[0];
    if(!file) return;
    const form = new FormData(); 
    form.append('file', file);
    try {
      const res = await fetch(`${API_BASE}/api/images/upload`, {
        method:'POST', 
        body:form, 
        headers:{'Authorization':'Bearer '+token}
      });
      if(res.ok){ loadImages(); }
      else { const j = await res.json(); setErr(j.message || 'Upload failed'); }
    } catch(e){
      setErr("Upload error: "+e.message);
    }
  }

  return (
    <div>
      <div className="mb-3">
        <input type="file" accept="image/png" onChange={upload} />
        <button className="btn btn-danger ms-2" onClick={onLogout}>Logout</button>
      </div>
      {err && <div className="alert alert-danger">{err}</div>}
      <div className="row">
        {images.map((img,i)=>
          <div className="col-3" key={i}>
            <img src={img.url} alt={img.filename} style={{width:'100%'}}/>
            <p>{img.filename}</p>
          </div>
        )}
      </div>
    </div>
  );
}
