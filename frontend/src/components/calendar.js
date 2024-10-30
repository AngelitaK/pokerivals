import { useNextCalendarApp, ScheduleXCalendar } from '@schedule-x/react'
import {
  createViewDay,
  createViewWeek,
  createViewMonthGrid,
  viewMonthGrid
} from '@schedule-x/calendar'
 
import '@schedule-x/theme-default/dist/index.css'
 
const Calendar = (events) => {
 
  const calendar = useNextCalendarApp({
    views: [createViewDay(), createViewWeek(), createViewMonthGrid()],
    events: events.events,
    defaultView: viewMonthGrid.name
  })
 
  return (
    <div>
      <ScheduleXCalendar calendarApp={calendar} />
    </div>
  )
}
 
export default Calendar