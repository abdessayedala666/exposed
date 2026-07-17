import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, Signal, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Client, IMessage } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
type SeatAction = {
  index: number;
  action: 'join' | 'leave';
};

type CardPayload = {
  number: number;
  suit: string;
};

type TeamDisplayNamesPayload = {
  team1: string[];
  team2: string[];
};

type TeamScoresPayload = {
  team1: number;
  team2: number;
};

type UnknownTeamDisplayNamesPayload = Partial<Record<string, string[]>>;
type UnknownTeamScoresPayload = Partial<Record<string, number>>;
type UnknownCapturedCardsByPlayerIndexPayload = Partial<Record<string, CardPayload[]>>;
type UnknownCardsNumberByPlayerIndexPayload = Partial<Record<string, number | null>>;
type MoveResult = {
  type?: string | null;
  roomId?: string | null;
  seat?: number | null;
  playerName?: string | null;
  hand: CardPayload[];
  capturedCardsByPlayerIndex?: Record<number, CardPayload[]> | null;
  currentPlayerName: string;
  topCardInPile: CardPayload | null;
  cardsNumberByPlayerIndex? : Record<number , number> | null ;
  roundNumber?: number | null;
  roundState?: string | null;
  setNumber?: number | null;
  setState?: string | null;
  gameState?: string | null;
  winnerTeam?: string | null;
  teamDisplayNames?: TeamDisplayNamesPayload | null;
  teamScores?: TeamScoresPayload | null;
};

type MovePayload = {
  card: CardPayload;
  action: 'CAPTURE_FROM_PILE' | 'CAPTURE_FROM_OPPONENT';
  facingSeat: number | null;
};

type MoveErrorPayload = {
  message?: string;
};

@Component({
  selector: 'app-room',
  imports: [CommonModule],
  templateUrl: './room.html',
  styleUrl: './room.css',
})
export class Room implements OnInit {
  private readonly http = inject(HttpClient);
  private client!: Client;
  private route = inject(ActivatedRoute);
  private type : string | undefined ;


  seats = signal<string[]>(['Player1', 'Player2', 'Player3', 'Player4']);
  disabledButtons = signal<boolean[]>([false, false, false, false]);
  roomId: string | null = null;
  myCards = signal<{ number: number; suit: string }[]>([]);
  currentPlayerName = signal('');
  draggedCard = signal<CardPayload | null>(null);
  draggedCardIndex = signal<number | null>(null);
  topCardInpile = signal<CardPayload | null>(null);
  roundNumber = signal<number | null>(null);
  roundState = signal<string | null>(null);
  setNumber = signal<number | null>(null);
  setState = signal<string | null>(null);
  gameState = signal<string | null>("DIDNT_START");
  cardsNumberByPlayerIndex = signal<Record<number , number>>({}) ;
  winnerTeam = signal<string | null>(null);
  moveError = signal<string | null>(null);
  capturedCardsByPlayerIndex = signal<Record<number, CardPayload[]>>({});
  teamDisplayNames = signal<TeamDisplayNamesPayload>({ team1: [], team2: [] });
  teamScores = signal<TeamScoresPayload>({ team1: 0, team2: 0 });

  private normalizeTeamDisplayNames(
    raw: TeamDisplayNamesPayload | UnknownTeamDisplayNamesPayload | null | undefined
  ): TeamDisplayNamesPayload {
    if (!raw) {
      return { team1: [], team2: [] };
    }

    const record = raw as UnknownTeamDisplayNamesPayload;
    return {
      team1: record['team1'] ?? record['Team1'] ?? record['TEAM1'] ?? [],
      team2: record['team2'] ?? record['Team2'] ?? record['TEAM2'] ?? [],
    };
  }

  private normalizeTeamScores(
    raw: TeamScoresPayload | UnknownTeamScoresPayload | null | undefined
  ): TeamScoresPayload {
    if (!raw) {
      return { team1: 0, team2: 0 };
    }

    const record = raw as UnknownTeamScoresPayload;
    return {
      team1: record['team1'] ?? record['Team1'] ?? record['TEAM1'] ?? 0,
      team2: record['team2'] ?? record['Team2'] ?? record['TEAM2'] ?? 0,
    };
  }
  private normalizeCardsNumberByPlayerIndex(
    raw: Record<number, number> | UnknownCardsNumberByPlayerIndexPayload | null | undefined
  ): Record<number, number> {
    if (!raw) {
      return {};
    }

    const record = raw as UnknownCardsNumberByPlayerIndexPayload;
    const normalized: Record<number, number> = {};

    Object.keys(record).forEach((key) => {
      const numericKey = Number(key);
      const value = record[key];
      if (!Number.isNaN(numericKey) && typeof value === 'number' && Number.isFinite(value)) {
        normalized[numericKey] = value;
      }
    });

    return normalized;
  }

  private normalizeCapturedCardsByPlayerIndex(
    raw: Record<number, CardPayload[]> | UnknownCapturedCardsByPlayerIndexPayload | null | undefined
  ): Record<number, CardPayload[]> {
    if (!raw) {
      return {};
    }

    const record = raw as UnknownCapturedCardsByPlayerIndexPayload;
    const normalized: Record<number, CardPayload[]> = {};
    Object.keys(record).forEach((key) => {
      const numericKey = Number(key);
      if (!Number.isNaN(numericKey)) {
        normalized[numericKey] = record[key] ?? [];
      }
    });
    return normalized;
  }

  getCapturedCardsForSeat(index: number): CardPayload[] {
    return this.capturedCardsByPlayerIndex()[index] ?? [];
  }
  getCardsCountForSeat(index : number) : number {
    return this.cardsNumberByPlayerIndex()[index] ?? 0 ;
  }
  createCardBackArray(count : number) : number[]{
    const safeCount = Number.isFinite(count)? Math.max(0 , Math.floor(count)) : 0 ; 
    return Array.from({length : safeCount} , (_,i) =>i) ;
  }

  getTeamNames(team: 'team1' | 'team2'): string {
    const names = this.teamDisplayNames()[team] ?? [];
    return names.length ? names.join(' & ') : 'N/A';
  }

  getTeamScore(team: 'team1' | 'team2'): number {
    return this.teamScores()[team] ?? 0;
  }

  name = '';
  roomOwner = signal('');

ngOnInit(): void {
  this.roomId = this.route.snapshot.paramMap.get('roomId');

  if (!this.roomId) {
    console.error('Room ID is missing');
    return;
  }

  this.loadMyName();
  this.loadSeats();
  this.getRoomOwner() ;
  this.client = new Client({
    brokerURL: `${environment.wsUrl}`,
    reconnectDelay: 5000,
    debug: (msg) => console.log('[STOMP]', msg),

    onConnect: () => {
      console.log('WebSocket connected');

      this.client.subscribe(`/topic/room/${this.roomId}`, (message: IMessage) => {
        const seats = JSON.parse(message.body) as string[];
        this.seats.set(seats);
        this.updateDisabledButtons(seats);
      });
      this.client.subscribe("/user/queue/game" , (message : IMessage)=>{
        const data = JSON.parse(message.body) as MoveResult;
        console.log('GAME LAUNCH PAYLOAD =', data);
        this.myCards.set(data.hand || []);
        this.capturedCardsByPlayerIndex.set(this.normalizeCapturedCardsByPlayerIndex(data.capturedCardsByPlayerIndex));
        this.cardsNumberByPlayerIndex.set(this.normalizeCardsNumberByPlayerIndex(data.cardsNumberByPlayerIndex) ) ;
        this.currentPlayerName.set(data.currentPlayerName || '');
        this.topCardInpile.set(data.topCardInPile || null);
        this.roundNumber.set(data.roundNumber ?? null);
        this.roundState.set(data.roundState ?? null);
        this.setNumber.set(data.setNumber ?? null);
        this.setState.set(data.setState ?? null);
        this.gameState.set(data.gameState ?? null);
        this.winnerTeam.set(data.winnerTeam ?? null);
        if (data.teamDisplayNames) {
          this.teamDisplayNames.set(this.normalizeTeamDisplayNames(data.teamDisplayNames));
        }
        if (data.teamScores) {
          this.teamScores.set(this.normalizeTeamScores(data.teamScores));
        }
        console.log('CURRENT PLAYER =', this.currentPlayerName());
        console.log('TOP CARD IN PILE =', this.topCardInpile());
      });
      this.client.subscribe("/user/queue/move", (message: IMessage) => {
        const data = JSON.parse(message.body) as MoveResult;
        console.log('GAME MOVE PAYLOAD =', data);
        
        if (data.hand !== null && data.hand !== undefined) {
          this.myCards.set(data.hand);
        }
        if (data.capturedCardsByPlayerIndex !== null && data.capturedCardsByPlayerIndex !== undefined) {
          this.capturedCardsByPlayerIndex.set(this.normalizeCapturedCardsByPlayerIndex(data.capturedCardsByPlayerIndex));
        }
        if (data.cardsNumberByPlayerIndex !== null && data.cardsNumberByPlayerIndex !== undefined) {
          this.cardsNumberByPlayerIndex.set(this.normalizeCardsNumberByPlayerIndex(data.cardsNumberByPlayerIndex));
        }
        
        if (data.currentPlayerName !== null && data.currentPlayerName !== undefined) {
          this.currentPlayerName.set(data.currentPlayerName);
        }
        
        this.topCardInpile.set(data.topCardInPile || null);
        this.roundNumber.set(data.roundNumber || null);
        this.roundState.set(data.roundState || null);
        this.setNumber.set(data.setNumber || null);
        this.setState.set(data.setState || null);
        this.gameState.set(data.gameState || null);
        this.winnerTeam.set(data.winnerTeam ?? null);
        if (data.teamDisplayNames) {
          this.teamDisplayNames.set(this.normalizeTeamDisplayNames(data.teamDisplayNames));
        }
        if (data.teamScores) {
          this.teamScores.set(this.normalizeTeamScores(data.teamScores));
        }
        
        console.log('FULL GAME STATE UPDATE =', {
          roundNumber: this.roundNumber(),
          roundState: this.roundState(),
          setNumber: this.setNumber(),
          setState: this.setState(),
          gameState: this.gameState(),
          winnerTeam: this.winnerTeam(),
          teamDisplayNames: this.teamDisplayNames(),
          teamScores: this.teamScores(),
          currentPlayer: this.currentPlayerName(),
          handSize: this.myCards().length,
          capturedCardsByPlayerIndex: this.capturedCardsByPlayerIndex(),
        });
      });

      this.client.subscribe("/user/queue/move-error", (message: IMessage) => {
        const errorPayload = JSON.parse(message.body) as MoveErrorPayload;
        const text = errorPayload.message || 'Invalid move';
        this.moveError.set(text);
        console.warn('MOVE ERROR =', text);
        window.setTimeout(() => this.moveError.set(null), 3000);
      });
    },

    onStompError: (frame) => {
      console.error('Broker error:', frame.headers['message']);
      console.error('Details:', frame.body);
    },

    onWebSocketError: (event) => {
      console.error('WebSocket error:', event);
    },
  });

  this.client.activate();
}
  getSuitSymbol(suit: string): string {
    switch (suit) {
      case 'SPADES': return '♠';
      case 'HEARTS': return '♥';
      case 'DIAMONDS': return '♦';
      case 'CLUBS': return '♣';
      default: return suit;
    }
  }
  getSuitColor(suit: string): string {
  switch (suit) {
    case 'HEARTS':
      return 'red';
    case 'DIAMONDS':
      return 'deeppink'; // nicer than pure red
    case 'SPADES':
    case 'CLUBS':
      return 'black';
    default:
      return 'black';
  }
}
getRoomOwner() {
  this.http.get(`${environment.apiUrl}/room/${this.roomId}/owner` ,{
    withCredentials: true ,
    responseType: 'text',
  })
  .subscribe({
    next : (data ) => {
      this.roomOwner.set(data) ;
      console.log(this.roomOwner()) ;
    },
    error : (error)=> {
      console.log(error) ;
    }
  })
}
loadSeats(): void {
  this.http.get<string[]>(`${environment.apiUrl}/room/${this.roomId}/seats`, {
    withCredentials: true,
  }).subscribe({
    next: (seats) => {
      this.seats.set(seats);
      this.updateDisabledButtons(seats);
    },
    error: (err) => {
      console.error('Failed to load seats', err);
    }
  });
}
  loadMyName(): void {
    this.http
      .post(`${environment.apiUrl}/room/me`, {}, {
        responseType: 'text',
        withCredentials: true,
      })
      .subscribe({
        next: (data) => {
          this.name = data;
          console.log(this.name);
          this.loadCurrentGameState();
        },
        error: (err) => console.error(err),
      });
  }

  loadCurrentGameState(): void {
    if (!this.roomId) {
      return;
    }

    this.http
      .get<MoveResult | null>(
        `${environment.apiUrl}/room/${this.roomId}/state`,
        { withCredentials: true }
      )
      .subscribe({
        next: (state) => {
          if (!state) {
            return;
          }

          this.myCards.set(state.hand || []);
          this.capturedCardsByPlayerIndex.set(this.normalizeCapturedCardsByPlayerIndex(state.capturedCardsByPlayerIndex));
          this.cardsNumberByPlayerIndex.set(this.normalizeCardsNumberByPlayerIndex(state.cardsNumberByPlayerIndex));
          this.currentPlayerName.set(state.currentPlayerName || '');
          this.topCardInpile.set(state.topCardInPile || null);
          this.roundNumber.set(state.roundNumber ?? null);
          this.roundState.set(state.roundState ?? null);
          this.setNumber.set(state.setNumber ?? null);
          this.setState.set(state.setState ?? null);
          this.gameState.set(state.gameState ?? null);
          this.winnerTeam.set(state.winnerTeam ?? null);
          if (state.teamDisplayNames) {
            this.teamDisplayNames.set(this.normalizeTeamDisplayNames(state.teamDisplayNames));
          }
          if (state.teamScores) {
            this.teamScores.set(this.normalizeTeamScores(state.teamScores));
          }
        },
        error: () => {
          // No active game state yet for this room/user.
        },
      });
  }



  updateDisabledButtons(seats: string[]): void {
    this.disabledButtons.set(
      seats.map((seat, index) => seat !== `Player${index + 1}`)
    );
  }

  sendSeatAction(index: number, action: 'join' | 'leave'): void {
    if (!this.client || !this.client.connected) {
      console.error('STOMP client is not connected');
      return;
    }

    const payload: SeatAction = { index, action };

    this.client.publish({
      destination: `/app/room/${this.roomId}/seats`,
      body: JSON.stringify(payload),
    });
  }

  reservePlace(index: number): void {
    this.sendSeatAction(index, 'join');
  }

  leavePlace(index: number): void {
    this.sendSeatAction(index, 'leave');
  }

  togglePlace(index: number): void {
    const current = this.seats()[index];

    if (current === `Player${index + 1}`) {
      this.reservePlace(index);
    } else if (current === this.name) {
      this.leavePlace(index);
    }
  }

  getPlayerClass(index: number): string {
    const team = index < 2 ? 'team1' : 'team2';
    const player = 'player' + (index + 1);
    return `${team} ${player}`;
  }

  launchGame(){
      const message = {
    type: 'LAUNCH_GAME'
  };
      this.client.publish({
      destination: `/app/room/${this.roomId}/launch`,
      body: JSON.stringify(message),
    });


  }

  onCardDragStart(event: DragEvent, card: CardPayload, cardIndex: number): void {
    if (!this.canCurrentPlayerThrow()) {
      event.preventDefault();
      return;
    }

    if (event.dataTransfer) {
      event.dataTransfer.setData('text/plain', JSON.stringify(card));
      event.dataTransfer.effectAllowed = 'move';
      const target = event.target as HTMLElement | null;
      if (target) {
        event.dataTransfer.setDragImage(target, target.clientWidth / 2, target.clientHeight / 2);
      }
    }
    this.draggedCard.set(card);
    this.draggedCardIndex.set(cardIndex);
  }

  onCardDragEnd(): void {
    this.draggedCard.set(null);
    this.draggedCardIndex.set(null);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  dropToCenter(event: DragEvent): void {
    event.preventDefault();
    if (!this.canCurrentPlayerThrow()) {
      return;
    }
    this.publishMove('CAPTURE_FROM_PILE', null);
  }

  dropToPlayer(event: DragEvent, targetSeat: number, targetPlayerName: string): void {
    event.preventDefault();
    if (!this.canCurrentPlayerThrow()) {
      return;
    }
    if (!targetPlayerName || targetPlayerName === this.name) {
      return;
    }
    if (targetPlayerName.startsWith('Player')) {
      return;
    }
    this.publishMove('CAPTURE_FROM_OPPONENT', targetSeat);
  }

  private publishMove(action: 'CAPTURE_FROM_PILE' | 'CAPTURE_FROM_OPPONENT', facingSeat: number | null): void {
    if (!this.canCurrentPlayerThrow()) {
      return;
    }

    if (!this.client || !this.client.connected || !this.roomId) {
      console.error('STOMP client is not connected');
      return;
    }

    const card = this.draggedCard();
    if (!card) {
      return;
    }

    const movePayload: MovePayload = {
      card,
      action,
      facingSeat,
    };

    this.client.publish({
      destination: `/app/room/${this.roomId}/move`,
      body: JSON.stringify(movePayload),
    });

    this.draggedCard.set(null);
    this.draggedCardIndex.set(null);
  }

  canCurrentPlayerThrow(): boolean {
    return !!this.name && this.currentPlayerName() === this.name;
  }
}