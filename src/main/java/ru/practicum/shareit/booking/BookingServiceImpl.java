package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.IncorrectStateException;
import ru.practicum.shareit.exceptions.ItemIsBookedException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UserIsNotOwnerException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Booking addBooking(Booking booking, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found", userId)));
        if (!booking.getItem().getAvailable()) {
            throw new ItemIsBookedException("Item is already reserved");
        }
        if (booking.getItem().getId().equals(userId)) {
            throw new NotFoundException("Owner can't bool his item");
        }
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long userId, Long bookingId, Boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id %d not found", bookingId)));
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found",
                        booking.getItem().getId())));
        if (!item.getOwnerId().equals(userId)) {
            throw new UserIsNotOwnerException("Only owner of item can approve booking");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ItemIsBookedException("You can change status only for waiting bookings");
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id %d not found", bookingId)));
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found",
                        booking.getItem().getId())));
        if (booking.getBooker().getId().equals(userId) || item.getOwnerId().equals(userId)) {
            return booking;
        } else {
            throw new UserIsNotOwnerException("Only owner of the item or booker can view information about booking");
        }
    }

    @Override
    public List<Booking> getAllUsersBookings(Long userId, State state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc
                        (userId, now, now);
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, now);
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId,
                        now);
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(userId,
                        now, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(userId, BookingStatus.REJECTED);
            default:
                throw new IncorrectStateException("Unknown state");
        }
    }

    @Override
    public List<Booking> getAllUsersItemsBookings(Long userId, State state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                return bookingRepository.getAllUsersItemsBookings(userId);
            case CURRENT:
                return bookingRepository.getCurrentUsersItemsBookings(userId, now);
            case PAST:
                return bookingRepository.getPastUsersItemsBookings(userId, now);
            case FUTURE:
                return bookingRepository.getFutureUsersItemsBookings(userId, now, BookingStatus.APPROVED);
            case WAITING:
                return bookingRepository.getWaitingUsersItemsBookings(userId, now, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.getRejectedUsersItemsBookings(userId, BookingStatus.REJECTED);
            default:
                throw new IncorrectStateException("Unknown state");
        }
    }
}