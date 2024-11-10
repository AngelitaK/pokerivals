package com.smu.csd.pokerivals.tournament.controller;

import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.tournament.service.TournamentPlayerService;
import com.smu.csd.pokerivals.tournament.dto.TournamentPageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/player/tournament")
@CrossOrigin
public class TournamentPlayerController {

    private final TournamentPlayerService tournamentPlayerService;

    public TournamentPlayerController(TournamentPlayerService tournamentPlayerService) {
        this.tournamentPlayerService = tournamentPlayerService;
    }

    @GetMapping("/me")
    @Operation(summary = "get all tournaments an player is participating in", description = "only for players")
    public TournamentPageDTO fetchMyTournaments(@AuthenticationPrincipal UserDetails userDetails,
                                                @Parameter(description = "page of tournament to get (start from zero)") @RequestParam("page") Integer page,
                                                @Parameter(description = "number of tournament per page") @RequestParam("limit") Integer pageSize){
        return tournamentPlayerService.getTournamentsByPlayer(userDetails.getUsername(),page, pageSize);
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "join a tournament", description = "only for players")
    public Message fetchMyTournaments(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable String id,
                                      @RequestBody TournamentPlayerService.JoinTournamentDTO dto){
        tournamentPlayerService.joinTournament(userDetails.getUsername(), UUID.fromString(id), dto);
        return new Message("Joined!");
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "leave a tournament", description = "only for players")
    public Message fetchMyTournaments(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable String id){
        tournamentPlayerService.leaveTournament(userDetails.getUsername(), UUID.fromString(id));
        return new Message("Left!");
    }

    @GetMapping("/search")
    @Operation(summary = "get tournaments based on name search and by player's elo", description = "only for players")
    public TournamentPageDTO searchTournaments(@AuthenticationPrincipal UserDetails userDetails,
                                               @Parameter(description = "page of tournament to get (start from zero)") @RequestParam("page") Integer page,
                                               @Parameter(description = "number of tournament per page") @RequestParam("limit") Integer pageSize,
                                               @Parameter(description = "case-insensitve name query") @RequestParam("query") String query){
        return tournamentPlayerService.searchTournaments(query,userDetails.getUsername(),page, pageSize);
    }

    @GetMapping("/closed/invited")
    @Operation(summary = "get CLOSED tournaments where player is invited", description = "only for players")
    public TournamentPageDTO findTournamentsWherePlayerInvited(@AuthenticationPrincipal UserDetails userDetails,
                                                               @Parameter(description = "page of tournament to get (start from zero)") @RequestParam("page") Integer page,
                                                               @Parameter(description = "number of tournament per page") @RequestParam("limit") Integer pageSize){
        return tournamentPlayerService.findTournamentWherePlayerIsInvited(userDetails.getUsername(),page, pageSize);
    }



}
