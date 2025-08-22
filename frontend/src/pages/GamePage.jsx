import React, { useState, useEffect, useCallback } from 'react';
import Board from '../components/Board';
import Keyboard from '../components/Keyboard';
import { createGame, makeGuess } from '../api/gameService';
import './GamePage.css';

const WORD_LENGTH = 5;
const MAX_TRIES = 6;

const GamePage = () => {
    const [game, setGame] = useState(null);
    const [currentGuess, setCurrentGuess] = useState('');
    const [error, setError] = useState('');
    const [usedLetters, setUsedLetters] = useState({});

    const startNewGame = useCallback(() => {
        createGame()
            .then(response => {
                setGame(response.data);
                setCurrentGuess('');
                setError('');
                setUsedLetters({});
            })
            .catch(err => setError("Не удалось начать новую игру."));
    }, []);

    useEffect(() => {
        startNewGame();
    }, [startNewGame]);

    const handleKeyPress = (key) => {
        if (game?.gameStatus !== 'PROCEED' || currentGuess.length >= WORD_LENGTH) {
            return;
        }
        setCurrentGuess(prev => prev + key);
    };

    const handleDelete = () => {
        setCurrentGuess(prev => prev.slice(0, -1));
    };

    const handleEnter = () => {
        if (game?.gameStatus !== 'PROCEED') return;
        if (currentGuess.length !== WORD_LENGTH) {
            setError(`Слово должно состоять из ${WORD_LENGTH} букв`);
            return;
        }

        makeGuess(game.id, currentGuess)
            .then(response => {
                const guessResult = response.data;
                const newAttempts = [...game.attempts, { guess: guessResult.guess, letterStatuses: guessResult.letterStatuses.map(s => s.toString()) }];

                setGame(prev => ({
                    ...prev,
                    attempts: newAttempts,
                    currentTry: guessResult.currentTry,
                    gameStatus: guessResult.gameStatus,
                    // ИЗМЕНЕНИЕ ЗДЕСЬ: Берем слово напрямую из ответа API,
                    // а не из предыдущего состояния, где его не было.
                    word: guessResult.word 
                }));

                const newUsedLetters = { ...usedLetters };
                guessResult.guess.split('').forEach((letter, index) => {
                    const status = guessResult.letterStatuses[index];
                    if (!newUsedLetters[letter] || status === 'CORRECT') {
                        newUsedLetters[letter] = status;
                    }
                });
                setUsedLetters(newUsedLetters);

                setCurrentGuess('');
                setError('');
            })
            .catch(err => setError(err.response?.data?.message || "Произошла ошибка"));
    };

    const handlePhysicalKeyboard = useCallback((event) => {
        if (event.key === 'Enter') {
            handleEnter();
        } else if (event.key === 'Backspace') {
            handleDelete();
        } else if (/^[a-zA-Z]$/.test(event.key)) {
            handleKeyPress(event.key.toUpperCase());
        }
    }, [handleEnter, handleDelete, handleKeyPress]);

    useEffect(() => {
        window.addEventListener('keydown', handlePhysicalKeyboard);
        return () => window.removeEventListener('keydown', handlePhysicalKeyboard);
    }, [handlePhysicalKeyboard]);

    if (!game) return <div>Загрузка...</div>;

    return (
        <div className="game-page">
            <h1>Wordle</h1>
            {error && <div className="error-message">{error}</div>}
            <Board
                attempts={game.attempts}
                currentTry={game.currentTry}
                currentGuess={currentGuess}
            />
            {game.gameStatus === 'WIN' && <div className="game-status win">Вы победили!</div>}
            {game.gameStatus === 'LOSE' && <div className="game-status lose">Вы проиграли! Загаданное слово: {game.word}</div>}

            {(game.gameStatus === 'WIN' || game.gameStatus === 'LOSE') && (
                <button onClick={startNewGame} className="new-game-btn">Новая игра</button>
            )}

            <Keyboard
                onKeyPress={handleKeyPress}
                onEnter={handleEnter}
                onDelete={handleDelete}
                usedLetters={usedLetters}
            />
        </div>
    );
};

export default GamePage;