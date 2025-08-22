import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './ProfilePage.css';

const ProfilePage = () => {
    const { user, logout, loading } = useAuth();
    const navigate = useNavigate();

    if (loading) {
        return <div>Загрузка профиля...</div>;
    }

    if (!user) {
        navigate('/login');
        return null;
    }

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <div className="profile-container">
            <h2>Профиль пользователя</h2>
            <p><strong>Имя пользователя:</strong> {user.username}</p>
            <p><strong>Имя:</strong> {user.firstName}</p>
            <p><strong>Фамилия:</strong> {user.lastName}</p>
            <p><strong>Победы:</strong> {user.wins}</p>
            <p><strong>Поражения:</strong> {user.losses}</p>
            <p><strong>Позиция в рейтинге:</strong> {user.position}</p>
            <button onClick={handleLogout}>Выйти</button>
        </div>
    );
};

export default ProfilePage;