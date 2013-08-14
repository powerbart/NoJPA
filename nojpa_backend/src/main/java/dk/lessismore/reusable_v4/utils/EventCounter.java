package dk.lessismore.reusable_v4.utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: bart
 * Date: 2007-02-28
 * Time: 12:40:54
 * To change this template use File | Settings | File Templates.
 */
public class EventCounter {

    HashMap<String, Event> events = new HashMap<String, Event>();
    long totalCounter = 0;

    public EventCounter(){

    }

    public class Event {

        public String key = null;
        public long totalTime = 1;
        public Integer countOfEvents = 0;


        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }

        public Integer getCountOfEvents() {
            return countOfEvents;
        }

        public void setCountOfEvents(Integer countOfEvents) {
            this.countOfEvents = countOfEvents;
        }
    }


    public synchronized long getTotalCountOfEvents() {
        return totalCounter;
    }

    public synchronized void newEvent(String  key, long time){
        totalCounter++;
        Event e = events.get(key);
        if(e == null){
            e = new Event();
            e.key = key;
            e.totalTime = time;
            events.put(key, e);
        } else {
            e.countOfEvents++;
            e.totalTime += time;
        }
    }

    public synchronized List<Event> getStatus(){
       List<Event> myList = new ArrayList<Event>(events.size());
       myList.addAll(events.values());
        Collections.sort(myList, new GenericComparator(EventCounter.Event.class, "key", true));
       Collections.sort(myList, new GenericComparator(EventCounter.Event.class, "countOfEvents", true));
       return myList;
    }

    public synchronized List<Event> getStatus(int maxCount){
        List<Event> myList = new ArrayList<Event>(events.size());
        Iterator<Event> iterator = events.values().iterator();
        for(int i = 0, size = events.size(); i < maxCount && iterator.hasNext(); i++){
            myList.add(iterator.next());      
        }
        Collections.sort(myList, new GenericComparator(Event.class, "countOfEvents", true));
        return myList;
    }

    public synchronized void cleanClounter(){
        events.clear();
    }
}
