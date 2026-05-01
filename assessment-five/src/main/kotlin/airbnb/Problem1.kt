package airbnb

import java.util.PriorityQueue

// Meeting Rooms III
// Medium–Hard Heap Greedy Sorting
// You are given n meeting rooms numbered 0 to n-1 and an array of meetings where meetings[i] = [start_i, end_i] represents the start and end time of the i-th meeting (end time is exclusive).

// Meeting allocation rules:

// 1. Each meeting uses the lowest-numbered available room.
// 2. If no room is free, the meeting is delayed until the earliest room becomes free — it retains its original duration.
// 3. When multiple rooms free up at the same time, pick the one with the lowest number.

// Return the room number that held the most meetings. If there is a tie, return the lowest-numbered room.

// Example 1
// n = 2, meetings = [[0,10],[1,5],[2,7],[3,4]] Explanation: t=0 → meeting [0,10] → room 0 (free). Used: {0} t=1 → meeting [1,5] → room 1 (free). Used: {0,1} t=2 → meeting [2,7] → no rooms free. Delayed. Earliest free: room 1 at t=5. Duration=5. Runs [5,10]. t=3 → meeting [3,4] → no rooms free. Delayed. Earliest free: room 0 at t=10 or room 1 at t=10. Pick room 0. Duration=1. Runs [10,11]. Counts: room 0 = 2, room 1 = 2 → return 0 (tie → lowest) Output: 0
// Example 2
// n = 3, meetings = [[1,20],[2,10],[3,5],[4,9],[6,8]] Output: 1
// Constraints
// 1 ≤ n ≤ 100 1 ≤ meetings.length ≤ 10^5 0 ≤ start_i < end_i ≤ 5 * 10^5
data class Meeting(
    val endTime: Int,
    val roomId: Int
)

fun meetingRoom(meetings: List<Pair<Int, Int>>): Int {
    val availableRooms = PriorityQueue<Int>().apply {
        (0..<meetings.size).forEach { offer(it) }
    }
    val activeMeetings = PriorityQueue(
        compareBy<Meeting> { it.endTime }.thenBy { it.roomId }
    )
    val meetingCount = IntArray(meetings.size) { 0 }
    val sorted = meetings
        .sortedBy { it.first }

    for (meeting in sorted) {
        while(activeMeetings.isNotEmpty() && activeMeetings.peek().endTime <= meeting.first) {
            availableRooms.offer(activeMeetings.poll().roomId)
        }

        if (availableRooms.isNotEmpty()) {
            val room = availableRooms.poll()
            meetingCount[room]++
            activeMeetings.offer(Meeting(
                endTime = meeting.second,
                roomId = room
            ))
        } else {
            val earliestMeeting = activeMeetings.poll()
            val duration = meeting.second - meeting.first
            val meeting = Meeting(
                endTime = earliestMeeting.endTime + duration,
                roomId = earliestMeeting.roomId
            )
            meetingCount[earliestMeeting.roomId]++
            activeMeetings.offer(meeting)
        }
    }

    return meetingCount.indices.maxByOrNull { meetingCount[it] } ?: 0
}

fun main() {
    println(
        meetingRoom(listOf(
            0 to 10,
            1 to 5,
            2 to 7,
            3 to 4
        ))
    )
}