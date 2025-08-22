import React from 'react';
import './Board.css';

const Board = ({ attempts, currentTry, currentGuess, wordLength = 5 }) => {
    const rows = Array.from({ length: 6 });

    return (
        <div className="board">
            {rows.map((_, rowIndex) => {
                const isCurrentRow = rowIndex === currentTry;
                const attempt = attempts[rowIndex];

                return (
                    <div key={rowIndex} className="board-row">
                        {Array.from({ length: wordLength }).map((_, colIndex) => {
                            let letter = '';
                            let status = '';

                            if (isCurrentRow) {
                                letter = currentGuess[colIndex] || '';
                            } else if (attempt) {
                                letter = attempt.guess[colIndex];
                                status = attempt.letterStatuses[colIndex]?.toLowerCase() || '';
                            }

                            return (
                                <div key={colIndex} className={`board-tile ${status}`}>
                                    {letter}
                                </div>
                            );
                        })}
                    </div>
                );
            })}
        </div>
    );
};

export default Board;