import { useNextCalendarApp, ScheduleXCalendar } from '@schedule-x/react';
import {
    createViewMonthGrid,
} from '@schedule-x/calendar';

import '@schedule-x/theme-default/dist/index.css';
import { createEventsServicePlugin } from '@schedule-x/events-service';
import { createCalendarControlsPlugin } from '@schedule-x/calendar-controls';
import { createEventModalPlugin } from '@schedule-x/event-modal';
import { useState, useEffect } from 'react';
import LoadingOverlay from './loadingOverlay';


const Calendar = ({ matches, onMonthChange }) => {

    if (matches.length != 0) {
        const [currentMonth, setCurrentMonth] = useState(null);

        const plugin = [createEventsServicePlugin(), createCalendarControlsPlugin(), createEventModalPlugin()];
        const calendar = useNextCalendarApp({
            views: [createViewMonthGrid()],
            defaultView: createViewMonthGrid().name,
            callbacks: {
                /**
                 * Is called when:
                 * 1. Selecting a date in the date picker
                 * 2. Selecting a new view
                 * */
                onRangeUpdate(range) {
                    onMonthChange(range.start)
                }
            }

        }, plugin);

        useEffect(() => {
            if (calendar) {
                calendar.eventsService.set(matches)
            }
        }, [matches]);

        return (
            <div>
                <ScheduleXCalendar calendarApp={calendar} />
            </div>
        );
    } else {
        return (
            <LoadingOverlay/>
        )
    }


};

export default Calendar;