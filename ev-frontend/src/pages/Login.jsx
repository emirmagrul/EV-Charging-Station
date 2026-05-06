import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [role, setRole] = useState('EV_DRIVER'); // Backend: EV_DRIVER, OPERATOR, ADMIN
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
    confirmPassword: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log(`${isLogin ? 'Giriş' : 'Kayıt'} yapılıyor:`, { ...formData, role });
    
    // Simüle edilmiş login işlemi
    const mockUser = {
      name: formData.name || formData.email.split('@')[0],
      email: formData.email,
      role: role
    };
    login(mockUser, 'mock-jwt-token');
    navigate('/dashboard');
  };

  return (
    <div className="login-page">
      <div className="login-container glass-panel">
        <div className="login-header">
          <h2>{isLogin ? 'Tekrar Hoş Geldin' : 'Aramıza Katıl'}</h2>
          <p>{isLogin ? 'Hesabına giriş yap ve enerji dolu yola devam et.' : 'Hemen kayıt ol, en yakın istasyonları keşfet.'}</p>
        </div>

        <div className="role-selector">
          <button 
            className={role === 'EV_DRIVER' ? 'active' : ''} 
            onClick={() => setRole('EV_DRIVER')}
          >
            Kullanıcı
          </button>
          <button 
            className={role === 'OPERATOR' ? 'active' : ''} 
            onClick={() => setRole('OPERATOR')}
          >
            Operatör
          </button>
          <button 
            className={role === 'ADMIN' ? 'active' : ''} 
            onClick={() => setRole('ADMIN')}
          >
            Admin
          </button>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          {!isLogin && (
            <div className="form-group">
              <label>Ad Soyad</label>
              <input 
                type="text" 
                name="name" 
                placeholder="Adınızı giriniz" 
                value={formData.name}
                onChange={handleChange}
                required 
              />
            </div>
          )}

          <div className="form-group">
            <label>E-posta</label>
            <input 
              type="email" 
              name="email" 
              placeholder="E-posta adresiniz" 
              value={formData.email}
              onChange={handleChange}
              required 
            />
          </div>

          <div className="form-group">
            <label>Şifre</label>
            <input 
              type="password" 
              name="password" 
              placeholder="••••••••" 
              value={formData.password}
              onChange={handleChange}
              required 
            />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label>Şifre Tekrar</label>
              <input 
                type="password" 
                name="confirmPassword" 
                placeholder="••••••••" 
                value={formData.confirmPassword}
                onChange={handleChange}
                required 
              />
            </div>
          )}

          <button type="submit" className="btn-primary-new login-submit">
            {isLogin ? 'Giriş Yap' : 'Kayıt Ol'}
          </button>
        </form>

        <div className="login-footer">
          <p>
            {isLogin ? 'Henüz hesabın yok mu?' : 'Zaten hesabın var mı?'}
            <span onClick={() => setIsLogin(!isLogin)}>
              {isLogin ? ' Kayıt Ol' : ' Giriş Yap'}
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
