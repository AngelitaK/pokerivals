import React, { useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction'; 

const Calendar = () => {
    // Sample events data
    const [events, setEvents] = useState([
        { id: '1', title: 'Meeting', start: '2023-11-01T10:00:00', end: '2023-11-01T12:00:00' },
        { id: '2', title: 'Conference', start: '2023-11-07', end: '2023-11-10', allDay: true },
        { id: '3', title: 'Lunch with Team', start: '2023-11-14T12:00:00', end: '2023-11-14T13:00:00' },
    ]);

    // Handle adding an event on date click
    const handleDateClick = (info) => {
        const newEventTitle = prompt("Enter event title:");
        if (newEventTitle) {
            const newEvent = {
                id: String(events.length + 1),
                title: newEventTitle,
                start: info.dateStr,
                allDay: true,
            };
            setEvents([...events, newEvent]);
        }
    };

    // Handle clicking on an event
    const handleEventClick = (info) => {
        const isConfirmed = window.confirm(`Delete event '${info.event.title}'?`);
        if (isConfirmed) {
            setEvents(events.filter((event) => event.id !== info.event.id));
        }
    };

    return (
        <div>
            <FullCalendar
                plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
                initialView="dayGridMonth"
                headerToolbar={{
                    left: 'prev,next today',
                    center: 'title',
                    right: 'dayGridMonth,timeGridWeek,timeGridDay',
                }}
                events={events}
                dateClick={handleDateClick}
                eventClick={handleEventClick}
                selectable
                editable
            />
        </div>
    );
};

export default Calendar;
