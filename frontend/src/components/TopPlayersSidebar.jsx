import React from 'react';
import './TopPlayersSidebar.css'; 

const TopPlayersSidebar = ({ players, loading, error }) => {
    
    if (loading) {
        return <div className="sidebar-message">Загрузка рейтинга...</div>;
    }

    if (error) {
        return <div className="sidebar-message error">{error}</div>;
    }

    if (!players || players.length === 0) {
        return <div className="sidebar-message">Рейтинг пока пуст.</div>;
    }

    return (
        <aside className="top-players-sidebar">
            <h3>Топ игроков</h3>
            <ol className="sidebar-list">
                {players.map((player, index) => (
                    <li key={player.username} className="sidebar-player-item">
                        <span className="sidebar-player-rank">{index + 1}.</span>
                        <span className="sidebar-player-username">{player.username}</span>
                        <span className="sidebar-player-wins">{player.wins}</span>
                    </li>
                ))}
            </ol>
        </aside>
    );
};

export default TopPlayersSidebar;