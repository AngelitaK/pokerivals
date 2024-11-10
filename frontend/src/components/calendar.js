import { useNextCalendarApp, ScheduleXCalendar } from '@schedule-x/react';
import {
    createViewDay,
    createViewWeek,
    createViewMonthGrid,
} from '@schedule-x/calendar';

import '@schedule-x/theme-default/dist/index.css';

const Calendar = ({ matches }) => {
    console.log(matches);
    const calendar = useNextCalendarApp({
        views: [createViewDay(), createViewWeek(), createViewMonthGrid()],
        events: matches,
        defaultView: createViewMonthGrid().name,
    });

    return (
        <div>
            <ScheduleXCalendar calendarApp={calendar} />
        </div>
    );
};

export default Calendar;