import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getMyRank } from '../api/authService';
import './ProfilePage.css';

const ProfilePage = () => {
    const { user, logout, loading, refreshUser, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const [rank, setRank] = useState(null);

    useEffect(() => {
        if (isAuthenticated) {
            refreshUser();
            getMyRank()
                .then(response => setRank(response.data.rank))
                .catch(error => {
                    console.error("Не удалось загрузить ранг:", error);
                    setRank('Ошибка');
                });
        }
    }, [isAuthenticated]);

    useEffect(() => {
        if (!loading && !isAuthenticated) {
            navigate('/login');
        }
    }, [loading, isAuthenticated, navigate]);

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    if (loading) {
        return <div>Загрузка...</div>;
    }
    
    if (isAuthenticated && user) {
        return (
            <div className="profile-container">
                <h2>Профиль пользователя</h2>
                <p><strong>Имя пользователя:</strong> {user.username}</p>
                <p><strong>Имя:</strong> {user.firstName}</p>
                <p><strong>Фамилия:</strong> {user.lastName}</p>
                <p><strong>Победы:</strong> {user.wins}</p>
                <p><strong>Поражения:</strong> {user.losses}</p>
                <p><strong>Позиция в рейтинге:</strong> {rank === null ? 'Загрузка...' : rank}</p>
                <button onClick={handleLogout} className="logout-btn">Выйти</button>
            </div>
        );
    }

    return null;
};

export default ProfilePage;