import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { login as loginUser, register as registerUser } from '../api/authService';
import { useNavigate } from 'react-router-dom';
import './AuthPage.css';

const LoginPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [error, setError] = useState('');
    const auth = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            let response;
            if (isLogin) {
                response = await loginUser({ username, password });
            } else {
                response = await registerUser({ username, password, firstName, lastName });
            }
            const { token } = response.data;
            await auth.login(token);
            navigate('/profile');

        } catch (err) {
            setError(err.response?.data?.message || 'Ошибка аутентификации. Проверьте данные.');
            localStorage.removeItem('token');
        }
    };

    return (
        <div className="auth-container">
            <form onSubmit={handleSubmit} className="auth-form">
                <h2>{isLogin ? 'Вход' : 'Регистрация'}</h2>
                {error && <p className="error">{error}</p>}
                {!isLogin && (
                    <>
                        <input type="text" value={firstName} onChange={e => setFirstName(e.target.value)} placeholder="Имя" required />
                        <input type="text" value={lastName} onChange={e => setLastName(e.target.value)} placeholder="Фамилия" required />
                    </>
                )}
                <input type="text" value={username} onChange={e => setUsername(e.target.value)} placeholder="Имя пользователя" required />
                <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Пароль" required />
                <button type="submit">{isLogin ? 'Войти' : 'Зарегистрироваться'}</button>
                <p onClick={() => setIsLogin(!isLogin)} className="toggle-form">
                    {isLogin ? 'Нет аккаунта? Зарегистрируйтесь' : 'Уже есть аккаунт? Войдите'}
                </p>
            </form>
        </div>
    );
};

export default LoginPage;