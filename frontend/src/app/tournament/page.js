// src/pages/TournamentPage.js
"use client"; // Marking this file as a client component

import React from 'react';
import { Bracket, Seed, SeedItem, SeedTeam, SeedTime, IRoundProps, IRenderSeedProps } from 'react-brackets';

// Simulated Pokémon tournament data
const rounds =  [
  {
    title: 'Round 1',
    seeds: [
      {
        id: 1,
        date: new Date().toDateString(),
        teams: [
          { id: 1, name: 'Pikachu', score: 0 },
          { id: 2, name: 'Charizard', score: 0 },
        ],
      },
      {
        id: 2,
        date: new Date().toDateString(),
        teams: [
          { id: 3, name: 'Bulbasaur', score: 0 },
          { id: 4, name: 'Squirtle', score: 0 },
        ],
      },
      {
        id: 3,
        date: new Date().toDateString(),
        teams: [
          { id: 5, name: 'Eevee', score: 0 },
          { id: 6, name: 'Jigglypuff', score: 0 },
        ],
      },
      {
        id: 4,
        date: new Date().toDateString(),
        teams: [
          { id: 7, name: 'Snorlax', score: 0 },
          { id: 8, name: 'Gengar', score: 0 },
        ],
      },
    ],
  },
  {
    title: 'Quarter Finals',
    seeds: [
      {
        id: 1,
        date: new Date().toDateString(),
        teams: [
          { id: 1, name: 'Pikachu', score: 0 },
          { id: 3, name: 'Bulbasaur', score: 0 },
        ],
      },
      {
        id: 2,
        date: new Date().toDateString(),
        teams: [
          { id: 2, name: 'Charizard', score: 0 },
          { id: 4, name: 'Squirtle', score: 0 },
        ],
      },
    ],
  },
  {
    title: 'Final',
    seeds: [
      {
        id: 1,
        date: new Date().toDateString(),
        teams: [
          { id: 1, name: 'Pikachu', score: 0 },
          { id: 5, name: 'Eevee', score: 0 },
        ],
      },
    ],
  },
];

const RenderSeed = ({ breakpoint, seed }) => {
  return (
    <Seed mobileBreakpoint={breakpoint}>
      <SeedItem style={{ width: '100%' }}>
        <div>
          <SeedTeam>{seed.teams?.[0]?.name || '-----------'}</SeedTeam>
          <div style={{ height: 1, backgroundColor: '#707070' }}></div>
          <SeedTeam>{seed.teams?.[1]?.name || '-----------'}</SeedTeam>
        </div>
      </SeedItem>
      <SeedTime mobileBreakpoint={breakpoint} style={{ fontSize: 9 }}>
        {seed.date}
      </SeedTime>
    </Seed>
  );
};

const TournamentPage = () => {
  return (
    <div style={{ padding: '20px', backgroundColor: '#f0f0f0', minHeight: '100vh' }}>
      <h1 style={{ textAlign: 'center', color: '#333' }}>Pokémon Tournament Bracket</h1>
      <Bracket
        mobileBreakpoint={767}
        rounds={rounds}
        renderSeedComponent={RenderSeed}
        swipeableProps={{ enableMouseEvents: true, animateHeight: true }}
      />
      <div style={{ textAlign: 'center', marginTop: '20px' }}>
        <h2 style={{ color: '#333' }}>Winner: Pikachu!</h2> {/* Announce the winner */}
      </div>
    </div>
  );
};

export default TournamentPage;
