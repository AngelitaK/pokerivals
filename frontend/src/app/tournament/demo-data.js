// src/demo-data.js
export default [
    {
      id: "1",
      name: "Round 1 - Match 1",
      sides: {
        home: { team: { id: "1", name: "Player 1" }, score: null },
        visitor: { team: { id: "2", name: "Player 2" }, score: null },
      },
    },
    {
      id: "2",
      name: "Round 1 - Match 2",
      sides: {
        home: { team: { id: "3", name: "Player 3" }, score: null },
        visitor: { team: { id: "4", name: "Player 4" }, score: null },
      },
    },
    {
      id: "3",
      name: "Round 1 - Match 3",
      sides: {
        home: { team: { id: "5", name: "Player 5" }, score: null },
        visitor: { team: { id: "6", name: "Player 6" }, score: null },
      },
    },
    {
      id: "4",
      name: "Round 1 - Match 4",
      sides: {
        home: { team: { id: "7", name: "Player 7" }, score: null },
        visitor: { team: { id: "8", name: "Player 8" }, score: null },
      },
    },
    {
      id: "5",
      name: "Semi Final - Match 1",
      sides: {
        home: { seed: { sourceGame: "1", rank: 1 }, score: null },
        visitor: { seed: { sourceGame: "2", rank: 1 }, score: null },
      },
    },
    {
      id: "6",
      name: "Semi Final - Match 2",
      sides: {
        home: { seed: { sourceGame: "3", rank: 1 }, score: null },
        visitor: { seed: { sourceGame: "4", rank: 1 }, score: null },
      },
    },
    {
      id: "7",
      name: "Final",
      sides: {
        home: { seed: { sourceGame: "5", rank: 1 }, score: null },
        visitor: { seed: { sourceGame: "6", rank: 1 }, score: null },
      },
    },
  ];
  