package aka.controller;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import aka.model.Schedule;
import aka.model.Teacher;
import aka.repository.ScheduleRepository;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherScheduleController {

    ScheduleRepository scheduleRepository;

    @GetMapping("/schedules")
    public String index(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Teacher teacher = SecurityUtils.getTeacher();
        Integer teacherId = teacher != null ? teacher.getId() : null;

        if (teacherId != null) {
            Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
            Page<Schedule> pageResult = scheduleRepository.findByTeacherId(teacherId, pageable);
            model.addAttribute("schedules", pageResult.getContent());
            model.addAttribute("pageObj", pageResult);
        } else {
            model.addAttribute("schedules", Collections.emptyList());
            model.addAttribute("pageObj", null);
        }

        return "teacher/schedule/list";
    }
}
