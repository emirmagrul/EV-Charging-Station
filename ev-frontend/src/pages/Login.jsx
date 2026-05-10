import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login: contextLogin } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [role, setRole] = useState('DRIVER'); 
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    confirmPassword: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isLogin && formData.password !== formData.confirmPassword) {
      return alert("Şifreler eşleşmiyor!");
    }

    setLoading(true);
    try {
      if (isLogin) {
        const userData = await authService.login(formData.email, formData.password, role);
        contextLogin(userData, 'mock-jwt-token');
        alert(`Hoş geldin, ${userData.firstName}!`);
        
        // Role göre yönlendirme
        if (role === 'DRIVER') navigate('/dashboard');
        else if (role === 'ADMIN') navigate('/admin-dashboard');
        else if (role === 'OPERATOR') navigate('/operator-dashboard');
        
      } else {
        const registerData = {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          password: formData.password,
          role: role // Seçilen rolü gönder
        };
        const userData = await authService.register(registerData);
        alert("Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
        setIsLogin(true);
      }
    } catch (error) {
      alert(error.message || "Giriş veya kayıt başarısız. Lütfen bilgilerinizi kontrol edin.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container glass-panel">
        <div className="login-header">
          <h2>{isLogin ? 'Tekrar Hoş Geldin' : 'Aramıza Katıl'}</h2>
          <p>{isLogin ? 'Hesabına giriş yap ve enerji dolu yola devam et.' : 'Hemen kayıt ol, en yakın istasyonları keşfet.'}</p>
        </div>

        <div className="role-selector">
          <button type="button" className={role === 'DRIVER' ? 'active' : ''} onClick={() => setRole('DRIVER')}>Kullanıcı</button>
          <button type="button" className={role === 'OPERATOR' ? 'active' : ''} onClick={() => setRole('OPERATOR')}>Operatör</button>
          <button type="button" className={role === 'ADMIN' ? 'active' : ''} onClick={() => setRole('ADMIN')}>Admin</button>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          {!isLogin && (
            <div className="form-row">
              <div className="form-group">
                <label>Ad</label>
                <input type="text" name="firstName" placeholder="Ad" value={formData.firstName} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Soyad</label>
                <input type="text" name="lastName" placeholder="Soyad" value={formData.lastName} onChange={handleChange} required />
              </div>
            </div>
          )}


          <div className="form-group">
            <label>E-posta</label>
            <input type="email" name="email" placeholder="E-posta" value={formData.email} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Şifre</label>
            <input type="password" name="password" placeholder="••••••••" value={formData.password} onChange={handleChange} required />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label>Şifre Tekrar</label>
              <input type="password" name="confirmPassword" placeholder="••••••••" value={formData.confirmPassword} onChange={handleChange} required />
            </div>
          )}

          <button type="submit" className="btn-primary-new login-submit" disabled={loading}>
            {loading ? 'İşlem yapılıyor...' : (isLogin ? 'Giriş Yap' : 'Kayıt Ol')}
          </button>
        </form>

        <div className="login-footer">
          <p>
            {isLogin ? 'Henüz hesabın yok mu?' : 'Zaten hesabın var mı?'}
            <span onClick={() => setIsLogin(!isLogin)} style={{cursor: 'pointer', color: 'var(--primary)', marginLeft: '5px'}}>
              {isLogin ? ' Kayıt Ol' : ' Giriş Yap'}
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
