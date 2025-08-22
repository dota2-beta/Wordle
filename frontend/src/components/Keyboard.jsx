import React from 'react';
import './Keyboard.css';

const Keyboard = ({ onKeyPress, onEnter, onDelete, usedLetters }) => {
    const keys = [
        ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
        ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'],
        ['ENTER', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', 'DELETE'],
    ];

    return (
        <div className="keyboard">
            {keys.map((row, rowIndex) => (
                <div key={rowIndex} className="keyboard-row">
                    {row.map((key) => {
                        const status = usedLetters[key] ? usedLetters[key].toLowerCase() : '';
                        return (
                            <button
                                key={key}
                                className={`key ${key.length > 1 ? 'special' : ''} ${status}`}
                                onClick={() => {
                                    if (key === 'ENTER') onEnter();
                                    else if (key === 'DELETE') onDelete();
                                    else onKeyPress(key);
                                }}
                            >
                                {key}
                            </button>
                        );
                    })}
                </div>
            ))}
        </div>
    );
};

export default Keyboard;