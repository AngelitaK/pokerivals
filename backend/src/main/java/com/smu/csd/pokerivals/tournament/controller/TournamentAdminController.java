package com.smu.csd.pokerivals.tournament.controller;

import com.smu.csd.pokerivals.record.Message;
import com.smu.csd.pokerivals.tournament.service.TournamentAdminService;
import com.smu.csd.pokerivals.tournament.dto.TournamentPageDTO;
import com.smu.csd.pokerivals.tournament.entity.OpenTournament;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/tournament")
@CrossOrigin
public class TournamentAdminController {

    private final TournamentAdminService tournamentAdminService;

    @Autowired
    public TournamentAdminController(TournamentAdminService tournamentAdminService) {
        this.tournamentAdminService = tournamentAdminService;
    }

    public record TournamentIDMessage(String message, UUID tournamentId){};

    @PostMapping("")
    @Operation(summary = "create an tournament (both open and closed)", description = "only for admins - use @type to indicate open or closed (same spelling)")
    public TournamentIDMessage createOpenTournament(@RequestBody @Valid Tournament tournament, @AuthenticationPrincipal UserDetails userDetails){
        UUID tournamentId = tournamentAdminService.createTournament(userDetails.getUsername(), tournament);
        return new TournamentIDMessage("Open tournament creation successful!", tournamentId);
    }

    @GetMapping("/me")
    @Operation(summary = "get all tournaments an admin is managing", description = "only for admins")
    public TournamentPageDTO fetchMyTournaments(@AuthenticationPrincipal UserDetails userDetails,
                                                @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
                                                @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize){
        return tournamentAdminService.getTournamentsByAdmin(userDetails.getUsername(),page, pageSize);
    }


    @PutMapping("/{id}")
    @Operation(summary = "modify a tournament (BOTH OPEN AND CLOSE)", description = "only for admins" +
            "NOTE: even though this accepts OPEN tournament, it also works for closed" +
            "name, description, max team capacity, and estimated tournament period can be modified freely" +
            " and registration period can only be modified during or before registration period")
    public Message createClosedTournament(@RequestBody @Valid OpenTournament openTournament, @PathVariable String id){
        tournamentAdminService.modifyTournament(UUID.fromString(id), openTournament);
        return new Message("Success!");
    }

    public record InviteDTO(List<String> usernames){};

    @PostMapping("/closed/{id}/invitation")
    @Operation(summary = "invite players", description = "only for admins")
    public Message createClosedTournament(@RequestBody InviteDTO dto, @PathVariable String id){
        tournamentAdminService.invitePlayerToClosedTournament(UUID.fromString(id),dto.usernames() );
        return new Message("Success!");
    }

    @GetMapping("/{id}/team")
    @Operation(summary = "get all teams of a tournament that an admin is managing", description = "only for admins")
    public TournamentAdminService.TeamPageDTO fetchTeamsOfMyTournament(@AuthenticationPrincipal UserDetails userDetails,
                                                                       @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
                                                                       @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize,
                                                                       @PathVariable String id){
        return tournamentAdminService.getTeamsOfATournament(UUID.fromString(id),userDetails.getUsername(),page,pageSize);
    }

    @DeleteMapping("/{id}/team/player/{playerUsername}")
    @Operation(summary = "get all teams of a tournament that an admin is managing", description = "only for admins")
    public Message deleteTeamOfMyTournament(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable String id,
                                            @PathVariable String playerUsername){
        tournamentAdminService.kickPlayerFromTournament(userDetails.getUsername(),playerUsername,UUID.fromString(id));

        return new Message("Successfully removed player");
    }


}
