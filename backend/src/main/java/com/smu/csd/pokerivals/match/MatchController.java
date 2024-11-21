package com.smu.csd.pokerivals.match;

import com.smu.csd.pokerivals.match.entity.MatchWrapper;
import com.smu.csd.pokerivals.record.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tournament/match")
@CrossOrigin
public class MatchController {

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    private MatchService matchService;

    @GetMapping("/{id}")
    @Operation(summary = "get all matches of a given tournament to display", description = "list of lists. the outer list ordered from the last round to the first round")
    public List<MatchWrapper.MatchRoundDTO> fetMatchesOfATournament(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String id){
        return matchService.generateFrontendFriendlyBrackets(UUID.fromString(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start tournament", description = "can only start a tournament whose registration period has passed")
    public Message startTournament(@PathVariable String id,@AuthenticationPrincipal UserDetails userDetails){
        matchService.startSingleEliminationTournament(UUID.fromString(id),userDetails.getUsername());
        return new Message("tournament started");
    }

    @PatchMapping("/result")
    @Operation(summary = "set match result to either TEAM A win, TEAM B win, or match CANCELLED")
    public Message setMatchResult(@RequestBody @Valid MatchService.SetMatchResultDTO dto,@AuthenticationPrincipal UserDetails userDetails){
        matchService.setMatchResult(dto, userDetails.getUsername());
        return new Message("Result set");
    }

    @DeleteMapping("/forfeit")
    @Operation(summary = "Forfeit a player from the match", description = "If forfeitTeamA is false, team A is forfeited o/w team B. You cannot forfeit when that team is not finalised")
    public Message forfeitPlayer(@RequestBody @Valid MatchService.ForfeitDTO dto,@AuthenticationPrincipal UserDetails userDetails){
        matchService.forfeit(dto, userDetails.getUsername());
        return new Message("Result set");
    }

    @PatchMapping("/timing")
    @Operation(summary = "Approve/Reject timing given by admin to players", description = "If forfeitTeamA is false, team A is forfeited o/w team B. You cannot forfeit when that team is not finalised")
    public Message approveOrRejectMatchTiming(@RequestBody @Valid MatchService.ApproveRejectMatchTimingDTO dto,@AuthenticationPrincipal UserDetails userDetail){
        matchService.approveOrRejectMatchTiming(dto,userDetail.getUsername());
        return new Message("Changes successful");
    }

    @PostMapping("/timing")
    @Operation(summary = "Suggest timing for a match by admin", description = "This will reset the approvals given by the players")
    public Message suggestMatchTiming(@RequestBody @Valid MatchService.ApproveRejectMatchTimingDTO dto,@AuthenticationPrincipal UserDetails userDetail){
        matchService.approveOrRejectMatchTiming(dto,userDetail.getUsername());
        return new Message("Timing set");
    }

    @GetMapping("/me/admin")
    @Operation(summary = "Get my (admin) matches between two times (i.e. point in time)")
    public MatchService.MatchPageDTO getMatchesForAdminBetweenDates(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "start of period to search ISO-8601 compliant") @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @Parameter(description = "end of period to search ISO-8601 compliant") @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
            @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize
    ) {
        return matchService.getMatchesForAdminBetweenDates(userDetails.getUsername(),start,end,page,pageSize);
    }

    @GetMapping("/me/player")
    @Operation(summary = "Get my (admin) matches between two times (i.e. point in time)")
    public MatchService.MatchPageDTO getMatchesForPlayerBetweenDates(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "start of period to search ISO-8601 compliant") @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @Parameter(description = "end of period to search ISO-8601 compliant") @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
            @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize
    ) {
        return matchService.getMatchesForPlayerBetweenDates(userDetails.getUsername(),start,end,page,pageSize);
    }

    @GetMapping("/timing")
    @Operation(summary = "Get any matches between two times (i.e. point in time)")
    public MatchService.MatchPageDTO getMatchesBetweenDates(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "start of period to search ISO-8601 compliant") @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @Parameter(description = "end of period to search ISO-8601 compliant") @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
            @Parameter(description = "page of pokemon to get (start from zero)") @RequestParam("page") Integer page,
            @Parameter(description = "number of pokemon per page") @RequestParam("limit") Integer pageSize
    ) {
        return matchService.getMatchesBetweenDates(start,end,page,pageSize);
    }
}
