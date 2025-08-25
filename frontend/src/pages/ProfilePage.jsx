import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getMyRank } from '../api/authService';
import './ProfilePage.css';

const ProfilePage = () => {
    const { user, logout, loading } = useAuth();
    const navigate = useNavigate();
    const [rank, setRank] = useState(null);
    useEffect(() => {
        if (user) {
            getMyRank()
                .then(response => {
                    setRank(response.data.rank);
                })
                .catch(error => {
                    console.error("Не удалось загрузить ранг пользователя:", error);
                    setRank('Ошибка');
                });
        }
    }, [user]);

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
            {rank !== null ? (
                <p><strong>Позиция в рейтинге:</strong> {rank}</p>
            ) : (
                <p><strong>Позиция в рейтинге:</strong> Загрузка...</p>
            )}

            <button onClick={handleLogout} className="logout-btn">Выйти</button>
        </div>
    );
};

export default ProfilePage;