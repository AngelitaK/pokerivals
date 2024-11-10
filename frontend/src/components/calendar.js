import { useNextCalendarApp, ScheduleXCalendar } from '@schedule-x/react';
import {
    createViewDay,
    createViewWeek,
    createViewMonthGrid,
} from '@schedule-x/calendar';

import '@schedule-x/theme-default/dist/index.css';
import { createEventsServicePlugin } from '@schedule-x/events-service';

const Calendar = ({ matches }) => {

    if (matches.length != 0) {
        
        const plugin = [createEventsServicePlugin()]

        const calendar = useNextCalendarApp({
            views: [createViewDay(), createViewWeek(), createViewMonthGrid()],
            events: matches,
            defaultView: createViewMonthGrid().name,
        }, plugin);

        return (
            <div>
                <ScheduleXCalendar calendarApp={calendar} />
            </div>
        );
    }

};

export default Calendar;